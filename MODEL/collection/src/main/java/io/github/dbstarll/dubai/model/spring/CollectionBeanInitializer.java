package io.github.dbstarll.dubai.model.spring;

import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.collection.CollectionFactory;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;

import java.io.IOException;

public final class CollectionBeanInitializer implements BeanDefinitionRegistryPostProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionBeanInitializer.class);

    private static final String CLASS_RESOURCE_PATTERN = "*.class";
    private static final String CLASS_RESOURCE_PATTERN_RECURSION = "**/*.class";

    private static final ResourcePatternResolver RESOURCE_RESOLVER = new PathMatchingResourcePatternResolver();
    private static final MetadataReaderFactory METADATA_FACTORY = new CachingMetadataReaderFactory(RESOURCE_RESOLVER);
    private static final TypeFilter TYPE_FILTER = new AssignableTypeFilter(Entity.class);

    private static final String COLLECTION_FACTORY_BEAN_NAME = CollectionFactory.class.getName();

    private String[] basePackages;
    private String mongoDatabaseBeanName;

    private boolean recursion;

    @Override
    public void postProcessBeanFactory(@NonNull final ConfigurableListableBeanFactory beanFactory)
            throws BeansException {
        // do nothing
    }

    @Override
    public void postProcessBeanDefinitionRegistry(@NonNull final BeanDefinitionRegistry registry)
            throws BeansException {
        createIfMissCollectionFactory(registry);
        if (basePackages != null) {
            for (String basePackage : basePackages) {
                try {
                    doScan(basePackage, registry);
                } catch (BeansException ex) {
                    throw ex;
                } catch (Exception ex) {
                    throw new BeanDefinitionStoreException("I/O failure during classpath scanning", ex);
                }
            }
        }
    }

    private void createIfMissCollectionFactory(final BeanDefinitionRegistry registry) throws BeansException {
        if (!registry.containsBeanDefinition(COLLECTION_FACTORY_BEAN_NAME)) {
            final BeanDefinition definition = BeanDefinitionBuilder
                    .genericBeanDefinition(CollectionFactory.class)
                    .setScope(BeanDefinition.SCOPE_SINGLETON)
                    .setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE)
                    .addConstructorArgReference(validateMongoDatabaseBeanName(registry))
                    .getBeanDefinition();
            LOGGER.info("registerBeanDefinition: [{}] with: {}", COLLECTION_FACTORY_BEAN_NAME, definition);
            registry.registerBeanDefinition(COLLECTION_FACTORY_BEAN_NAME, definition);
        }
    }

    private String validateMongoDatabaseBeanName(final BeanDefinitionRegistry registry) throws BeansException {
        if (!StringUtils.isBlank(mongoDatabaseBeanName)) {
            if (registry.containsBeanDefinition(mongoDatabaseBeanName)) {
                return mongoDatabaseBeanName;
            } else {
                throw new NoSuchBeanDefinitionException(mongoDatabaseBeanName);
            }
        } else {
            throw new BeanDefinitionValidationException("mongoDatabaseBeanName not set.");
        }
    }

    private void doScan(final String basePackage, final BeanDefinitionRegistry registry)
            throws IOException, ClassNotFoundException {
        final String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                + ClassUtils.convertClassNameToResourcePath(basePackage) + '/'
                + (recursion ? CLASS_RESOURCE_PATTERN_RECURSION : CLASS_RESOURCE_PATTERN);
        for (Resource resource : RESOURCE_RESOLVER.getResources(packageSearchPath)) {
            final MetadataReader metadataReader = METADATA_FACTORY.getMetadataReader(resource);
            if (TYPE_FILTER.match(metadataReader, METADATA_FACTORY)) {
                final Class<?> entityClass = Class.forName(metadataReader.getClassMetadata().getClassName());
                if (EntityFactory.isEntityClass(entityClass)) {
                    registerBeanDefinition(registry, entityClass);
                }
            }
        }
    }

    private void registerBeanDefinition(final BeanDefinitionRegistry registry, final Class<?> entityClass) {
        final ResolvableType targetType = ResolvableType.forClassWithGenerics(Collection.class, entityClass);
        final String beanName = targetType.toString();
        if (registry.containsBeanDefinition(beanName)) {
            throw new BeanDefinitionValidationException("collection already exist: " + beanName);
        } else {
            final BeanDefinition definition = buildCollection(entityClass, targetType);
            LOGGER.info("register collection[{}] of entity: {}", beanName, entityClass);
            registry.registerBeanDefinition(beanName, definition);
        }
    }

    /**
     * 判定一个BeanDefinition是否是一个匹配实体类的CollectionBeanDefinition.
     *
     * @param definition  待检查的BeanDefinition
     * @param entityClass 待匹配的实体类
     * @return 返回是否匹配
     */
    public static boolean isCollectionBeanDefinition(final BeanDefinition definition, final Class<?> entityClass) {
        final ResolvableType beanType = definition.getResolvableType();
        return Collection.class == beanType.resolve() && entityClass == beanType.resolveGeneric();
    }

    private BeanDefinition buildCollection(final Class<?> entityClass, final ResolvableType targetType) {
        final AbstractBeanDefinition bd = BeanDefinitionBuilder.rootBeanDefinition(Collection.class)
                .setFactoryMethodOnBean("newInstance", COLLECTION_FACTORY_BEAN_NAME)
                .setScope(BeanDefinition.SCOPE_SINGLETON)
                .addConstructorArgValue(entityClass)
                .getBeanDefinition();
        ((RootBeanDefinition) bd).setTargetType(targetType);
        return bd;
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
     * 设置是否递归检测基础包下的子包.
     *
     * @param recursion 是否递归检测子包
     */
    public void setRecursion(final boolean recursion) {
        this.recursion = recursion;
    }
}
