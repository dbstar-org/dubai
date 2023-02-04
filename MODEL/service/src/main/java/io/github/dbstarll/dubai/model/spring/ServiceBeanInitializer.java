package io.github.dbstarll.dubai.model.spring;

import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.service.Service;
import io.github.dbstarll.dubai.model.service.ServiceFactory;
import io.github.dbstarll.utils.lang.wrapper.EntryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
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
import java.util.Map.Entry;

public final class ServiceBeanInitializer implements BeanDefinitionRegistryPostProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBeanInitializer.class);

    private static final String CLASS_RESOURCE_PATTERN = "*.class";
    private static final String CLASS_RESOURCE_PATTERN_RECURSION = "**/*.class";

    private static final ResourcePatternResolver RESOURCE_RESOLVER = new PathMatchingResourcePatternResolver();
    private static final MetadataReaderFactory METADATA_FACTORY = new CachingMetadataReaderFactory(RESOURCE_RESOLVER);
    private static final TypeFilter TYPE_FILTER = new AssignableTypeFilter(Service.class);

    private String[] basePackages;

    private boolean recursion;

    @Override
    public void postProcessBeanFactory(@NonNull final ConfigurableListableBeanFactory beanFactory)
            throws BeansException {
        // do nothing
    }

    @Override
    public void postProcessBeanDefinitionRegistry(@NonNull final BeanDefinitionRegistry registry)
            throws BeansException {
        if (basePackages != null) {
            for (String basePackage : basePackages) {
                try {
                    doScan(basePackage, registry);
                } catch (BeansException ex) {
                    throw ex;
                } catch (Exception ex) {
                    throw new BeanDefinitionStoreException("failure during classpath scanning: " + basePackage, ex);
                }
            }
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
                final Class<?> serviceClass = Class.forName(metadataReader.getClassMetadata().getClassName());
                if (ServiceFactory.isServiceClass(serviceClass)) {
                    registerService((Class<? extends Service<Entity>>) serviceClass, registry);
                }
            }
        }
    }

    private <E extends Entity, S extends Service<E>> void registerService(final Class<S> serviceClass,
                                                                          final BeanDefinitionRegistry registry) {
        final Class<E> entityClass = ServiceFactory.getEntityClass(serviceClass);
        if (entityClass != null) {
            registerBeanDefinition(registry, entityClass, serviceClass, getServiceBeanName(serviceClass), 0);
        }
    }

    private <E extends Entity, S extends Service<E>> void registerBeanDefinition(
            final BeanDefinitionRegistry registry, final Class<E> entityClass, final Class<S> serviceClass,
            final String baseBeanName, final int index) {
        final String beanName = baseBeanName + (index > 0 ? index + 1 : "");
        if (registry.containsBeanDefinition(beanName)) {
            final BeanDefinition definition = registry.getBeanDefinition(beanName);
            if (isServiceBeanDefinition(definition, serviceClass)) {
                throw new BeanDefinitionValidationException(
                        "service already exist: [" + beanName + "]" + serviceClass);
            }
            LOGGER.warn("bean already exist: [{}] with definition: {}", beanName, definition);
            registerBeanDefinition(registry, entityClass, serviceClass, baseBeanName, index + 1);
        } else {
            final Entry<String, BeanDefinition> match = findCollectionBeanDefinition(registry, entityClass);
            if (match != null) {
                final BeanDefinition definition = buildService(serviceClass, match.getKey());
                LOGGER.info("register service[{}] of entity: {} with: {}", beanName, entityClass, serviceClass);
                registry.registerBeanDefinition(beanName, definition);
            } else {
                LOGGER.warn("collection not found for entity: {}, can't build service: {}", entityClass, serviceClass);
            }
        }
    }

    private Entry<String, BeanDefinition> findCollectionBeanDefinition(final BeanDefinitionRegistry registry,
                                                                       final Class<?> entityClass) {
        for (final String beanName : registry.getBeanDefinitionNames()) {
            final BeanDefinition definition = registry.getBeanDefinition(beanName);
            if (CollectionBeanInitializer.isCollectionBeanDefinition(definition, entityClass)) {
                return EntryWrapper.wrap(beanName, definition);
            }
        }
        return null;
    }

    private <E extends Entity, S extends Service<E>> String getServiceBeanName(final Class<S> serviceClass) {
        return StringUtils.uncapitalize(serviceClass.getSimpleName());
    }

    private boolean isServiceBeanDefinition(final BeanDefinition definition, final Class<?> serviceClass) {
        if (ServiceFactory.class.getName().equals(definition.getBeanClassName())) {
            final ValueHolder v = definition.getConstructorArgumentValues().getIndexedArgumentValue(0, null);
            return v != null && v.getValue() == serviceClass;
        }
        return false;
    }

    private <E extends Entity, S extends Service<E>> BeanDefinition buildService(
            final Class<S> serviceClass, final String collectionBeanName) {
        return BeanDefinitionBuilder.genericBeanDefinition(ServiceFactory.class)
                .setFactoryMethod("newInstance")
                .setScope(BeanDefinition.SCOPE_SINGLETON)
                .setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_NAME)
                .addConstructorArgValue(serviceClass)
                .addConstructorArgReference(collectionBeanName)
                .getBeanDefinition();
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
     * 设置是否递归检测基础包下的子包.
     *
     * @param recursion 是否递归检测子包
     */
    public void setRecursion(final boolean recursion) {
        this.recursion = recursion;
    }
}
