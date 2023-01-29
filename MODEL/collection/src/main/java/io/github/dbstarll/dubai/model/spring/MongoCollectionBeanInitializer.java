package io.github.dbstarll.dubai.model.spring;

import io.github.dbstarll.dubai.model.collection.CollectionFactory;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.ClassUtils;

import java.io.IOException;

public final class MongoCollectionBeanInitializer implements BeanDefinitionRegistryPostProcessor {
    private static final Logger LOGER = LoggerFactory.getLogger(MongoCollectionBeanInitializer.class);

    private static final String CLASS_RESOURCE_PATTERN = "*.class";
    private static final String CLASS_RESOURCE_PATTERN_RECURSION = "**/*.class";

    private static final ResourcePatternResolver RESOURCE_RESOLVER = new PathMatchingResourcePatternResolver();
    private static final MetadataReaderFactory METADATA_FACTORY = new CachingMetadataReaderFactory(RESOURCE_RESOLVER);
    private static final TypeFilter TYPE_FILTER = new AssignableTypeFilter(Entity.class);

    private String[] basePackages;
    private String mongoDatabaseBeanName;
    private String collectionFactoryBeanName = StringUtils.uncapitalize(CollectionFactory.class.getSimpleName());

    private boolean recursion;

    @Override
    public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // do nothing
    }

    @Override
    public void postProcessBeanDefinitionRegistry(final BeanDefinitionRegistry registry) throws BeansException {
        createIfMissCollectionFactory(registry);
        for (String basePackage : basePackages) {
            try {
                doScan(basePackage, registry);
            } catch (BeansException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new BeanDefinitionStoreException("I/O failure during classpath scanning", ex);
            }
        }
    }

    private void createIfMissCollectionFactory(final BeanDefinitionRegistry registry) throws BeansException {
        if (!registry.containsBeanDefinition(collectionFactoryBeanName)) {
            final GenericBeanDefinition definition = new GenericBeanDefinition();
            definition.setBeanClass(CollectionFactory.class);
            definition.setScope(BeanDefinition.SCOPE_SINGLETON);
            final ConstructorArgumentValues argumentValues = new ConstructorArgumentValues();
            argumentValues.addIndexedArgumentValue(0, new RuntimeBeanReference(mongoDatabaseBeanName));
            definition.setConstructorArgumentValues(argumentValues);
            definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_NAME);
            LOGER.info("registerBeanDefinition: [{}] with: {}", collectionFactoryBeanName, definition);
            registry.registerBeanDefinition(collectionFactoryBeanName, definition);
        }
    }

    @SuppressWarnings("unchecked")
    private void doScan(final String basePackage, final BeanDefinitionRegistry registry)
            throws IOException, ClassNotFoundException {
        final String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                + ClassUtils.convertClassNameToResourcePath(basePackage) + '/'
                + (recursion ? CLASS_RESOURCE_PATTERN_RECURSION : CLASS_RESOURCE_PATTERN);
        for (Resource resource : RESOURCE_RESOLVER.getResources(packageSearchPath)) {
            final MetadataReader metadataReader = METADATA_FACTORY.getMetadataReader(resource);
            if (TYPE_FILTER.match(metadataReader, METADATA_FACTORY)) {
                final Class<? extends Entity> entityClass = (Class<? extends Entity>) Class
                        .forName(metadataReader.getClassMetadata().getClassName());
                if (EntityFactory.isEntityClass(entityClass)) {
                    final String beanName = StringUtils.uncapitalize(entityClass.getSimpleName()) + "Collection";
                    if (registry.containsBeanDefinition(beanName)) {
                        throw new BeanDefinitionValidationException(
                                "collection already exist: [" + beanName + "]" + entityClass);
                    } else {
                        final BeanDefinition definition = buildCollection(entityClass);
                        LOGER.info("register collection[{}] of entity: {}", beanName, entityClass);
                        registry.registerBeanDefinition(beanName, definition);
                    }
                }
            }
        }
    }

    private BeanDefinition buildCollection(final Class<? extends Entity> entityClass) {
        final GenericBeanDefinition definition = new GenericBeanDefinition();
        definition.setFactoryBeanName(collectionFactoryBeanName);
        definition.setFactoryMethodName("newInstance");
        definition.setScope(BeanDefinition.SCOPE_SINGLETON);
        final ConstructorArgumentValues argumentValues = new ConstructorArgumentValues();
        argumentValues.addIndexedArgumentValue(0, entityClass);
        definition.setConstructorArgumentValues(argumentValues);
        definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_NAME);
        return definition;
    }

    /**
     * 根据基础类来转换基础包.
     *
     * @param basePackageClasses 基础类
     */
    public void setBasePackageClasses(final Class<?>... basePackageClasses) {
        this.basePackages = new String[basePackageClasses.length];
        for (int i = 0; i < basePackageClasses.length; i++) {
            this.basePackages[i] = basePackageClasses[i].getPackage().getName();
        }
    }

    /**
     * 设置基础包.
     *
     * @param basePackages 基础包
     */
    public void setBasePackages(final String... basePackages) {
        this.basePackages = basePackages;
    }

    /**
     * 设置mongoDatabase实例在spring context中的Bean名称.
     *
     * @param mongoDatabaseBeanName mongoDatabase实例的Bean名称
     */
    public void setMongoDatabaseBeanName(final String mongoDatabaseBeanName) {
        this.mongoDatabaseBeanName = mongoDatabaseBeanName;
    }

    /**
     * 设置collectionFactory实例在spring context中的Bean名称.
     *
     * @param collectionFactoryBeanName collectionFactory实例的Bean名称
     */
    public void setCollectionFactoryBeanName(final String collectionFactoryBeanName) {
        this.collectionFactoryBeanName = collectionFactoryBeanName;
    }

    /**
     * 设置是否递归检测基础包下的子包.
     *
     * @param recursion 是否递归检测子包
     */
    public void setRecursion(final boolean recursion) {
        this.recursion = recursion;
    }
}
