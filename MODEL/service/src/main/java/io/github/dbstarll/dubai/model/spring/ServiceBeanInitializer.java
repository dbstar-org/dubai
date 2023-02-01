package io.github.dbstarll.dubai.model.spring;

import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.service.Service;
import io.github.dbstarll.dubai.model.service.ServiceFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanInitializationException;
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
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

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
        if (basePackages == null || basePackages.length == 0) {
            throw new BeanInitializationException("basePackages not set.");
        }
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

    @SuppressWarnings("unchecked")
    private void doScan(final String basePackage, final BeanDefinitionRegistry registry)
            throws IOException, ClassNotFoundException {
        final String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                + ClassUtils.convertClassNameToResourcePath(basePackage) + '/'
                + (recursion ? CLASS_RESOURCE_PATTERN_RECURSION : CLASS_RESOURCE_PATTERN);
        for (Resource resource : RESOURCE_RESOLVER.getResources(packageSearchPath)) {
            final MetadataReader metadataReader = METADATA_FACTORY.getMetadataReader(resource);
            if (TYPE_FILTER.match(metadataReader, METADATA_FACTORY)) {
                final Class<? extends Service<Entity>> serviceClass = (Class<? extends Service<Entity>>) Class
                        .forName(metadataReader.getClassMetadata().getClassName());
                registerService(serviceClass, registry);
            }
        }
    }

    private <E extends Entity, S extends Service<E>> void registerService(final Class<S> serviceClass,
                                                                          final BeanDefinitionRegistry registry) {
        if (ServiceFactory.isServiceClass(serviceClass)) {
            final Class<E> entityClass = getEntityClass(serviceClass);
            if (entityClass != null) {
                final String serviceBeanName = getServiceBeanName(serviceClass);
                if (registry.containsBeanDefinition(serviceBeanName)) {
                    throw new BeanDefinitionValidationException(
                            "service already exist: [" + serviceBeanName + "]" + serviceClass);
                }
                final String collectionBeanName = StringUtils.uncapitalize(entityClass.getSimpleName()) + "Collection";
                final BeanDefinition definition = buildService(serviceClass, collectionBeanName);
                LOGGER.info("register service[{}] of entity: {} with: {}", serviceBeanName, entityClass, serviceClass);
                registry.registerBeanDefinition(serviceBeanName, definition);
            }
        }
    }

    private <E extends Entity, S extends Service<E>> Class<E> getEntityClass(final Class<S> serviceClass) {
        final Type genericSuperclass = serviceClass.getGenericSuperclass();
        if (genericSuperclass != null) {
            final Class<E> entityClass = getEntityClassFromGeneric(genericSuperclass);
            if (entityClass != null) {
                return entityClass;
            }
        }
        for (Type genericInterface : serviceClass.getGenericInterfaces()) {
            final Class<E> entityClass = getEntityClassFromGeneric(genericInterface);
            if (entityClass != null) {
                return entityClass;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <E extends Entity> Class<E> getEntityClassFromGeneric(final Type genericType) {
        if (genericType instanceof ParameterizedType) {
            for (Type type : ((ParameterizedType) genericType).getActualTypeArguments()) {
                if (EntityFactory.isEntityClass((Class<?>) type)) {
                    return (Class<E>) type;
                }
            }
        }
        return null;
    }

    private <E extends Entity, S extends Service<E>> String getServiceBeanName(final Class<S> serviceClass) {
        return StringUtils.uncapitalize(serviceClass.getSimpleName());
    }

    private <E extends Entity, S extends Service<E>> BeanDefinition buildService(
            final Class<S> serviceClass, final String collectionFactoryBeanName) {
        final GenericBeanDefinition definition = new GenericBeanDefinition();
        definition.setBeanClass(ServiceFactory.class);
        definition.setFactoryMethodName("newInstance");
        definition.setScope(BeanDefinition.SCOPE_SINGLETON);
        final ConstructorArgumentValues argumentValues = new ConstructorArgumentValues();
        argumentValues.addIndexedArgumentValue(0, serviceClass);
        argumentValues.addIndexedArgumentValue(1, new RuntimeBeanReference(collectionFactoryBeanName));
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
     * 设置是否递归检测基础包下的子包.
     *
     * @param recursion 是否递归检测子包
     */
    public void setRecursion(final boolean recursion) {
        this.recursion = recursion;
    }
}
