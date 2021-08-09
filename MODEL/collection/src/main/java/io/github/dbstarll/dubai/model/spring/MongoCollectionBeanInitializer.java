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
import org.springframework.beans.factory.support.*;
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

public class MongoCollectionBeanInitializer implements BeanDefinitionRegistryPostProcessor {
    private static final Logger LOGER = LoggerFactory.getLogger(MongoCollectionBeanInitializer.class);

    private static final String CLASS_RESOURCE_PATTERN = "*.class";
    private static final String CLASS_RESOURCE_PATTERN_RECURSION = "**/*.class";

    private static final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
    private static final MetadataReaderFactory metadataFactory = new CachingMetadataReaderFactory(resourceResolver);
    private static final TypeFilter typeFilter = new AssignableTypeFilter(Entity.class);

    private String[] basePackages;
    private String mongoDatabaseBeanName;
    private String collectionFactoryBeanName = StringUtils.uncapitalize(CollectionFactory.class.getSimpleName());

    private boolean recursion;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // do nothing
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
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

    private void createIfMissCollectionFactory(BeanDefinitionRegistry registry) throws BeansException {
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
    private void doScan(String basePackage, BeanDefinitionRegistry registry) throws IOException, ClassNotFoundException {
        final String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                + ClassUtils.convertClassNameToResourcePath(basePackage) + '/'
                + (recursion ? CLASS_RESOURCE_PATTERN_RECURSION : CLASS_RESOURCE_PATTERN);
        for (Resource resource : resourceResolver.getResources(packageSearchPath)) {
            final MetadataReader metadataReader = metadataFactory.getMetadataReader(resource);
            if (typeFilter.match(metadataReader, metadataFactory)) {
                final Class<? extends Entity> entityClass = (Class<? extends Entity>) Class
                        .forName(metadataReader.getClassMetadata().getClassName());
                if (EntityFactory.isEntityClass(entityClass)) {
                    final String collectionBeanName = StringUtils.uncapitalize(entityClass.getSimpleName()) + "Collection";
                    if (registry.containsBeanDefinition(collectionBeanName)) {
                        throw new BeanDefinitionValidationException(
                                "collection already exist: [" + collectionBeanName + "]" + entityClass);
                    } else {
                        final BeanDefinition definition = buildCollection(entityClass);
                        LOGER.info("register collection[{}] of entity: {}", collectionBeanName, entityClass);
                        registry.registerBeanDefinition(collectionBeanName, definition);
                    }
                }
            }
        }
    }

    private BeanDefinition buildCollection(Class<? extends Entity> entityClass) {
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
    public void setBasePackageClasses(Class<?>... basePackageClasses) {
        this.basePackages = new String[basePackageClasses.length];
        for (int i = 0; i < basePackageClasses.length; i++) {
            this.basePackages[i] = basePackageClasses[i].getPackage().getName();
        }
    }

    public void setBasePackages(String... basePackages) {
        this.basePackages = basePackages;
    }

    public void setMongoDatabaseBeanName(String mongoDatabaseBeanName) {
        this.mongoDatabaseBeanName = mongoDatabaseBeanName;
    }

    public void setCollectionFactoryBeanName(String collectionFactoryBeanName) {
        this.collectionFactoryBeanName = collectionFactoryBeanName;
    }

    public void setRecursion(boolean recursion) {
        this.recursion = recursion;
    }
}
