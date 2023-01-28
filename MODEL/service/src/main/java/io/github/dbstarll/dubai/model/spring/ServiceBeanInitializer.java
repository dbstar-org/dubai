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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ServiceBeanInitializer implements BeanDefinitionRegistryPostProcessor {
    private static final Logger LOGER = LoggerFactory.getLogger(ServiceBeanInitializer.class);

    private static final String CLASS_RESOURCE_PATTERN = "*.class";
    private static final String CLASS_RESOURCE_PATTERN_RECURSION = "**/*.class";

    private static final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
    private static final MetadataReaderFactory metadataFactory = new CachingMetadataReaderFactory(resourceResolver);
    private static final TypeFilter typeFilter = new AssignableTypeFilter(Service.class);

    private String[] basePackages;

    private boolean recursion;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // do nothing
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        if (basePackages == null || basePackages.length == 0) {
            throw new BeanInitializationException("basePackages not set.");
        }
        for (String basePackage : basePackages) {
            try {
                doScan(basePackage, registry);
            } catch (BeansException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new BeanDefinitionStoreException("failure during classpath scanning: " + basePackage, ex);
            }
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
                final Class<? extends Service<Entity>> serviceClass = (Class<? extends Service<Entity>>) Class
                        .forName(metadataReader.getClassMetadata().getClassName());
                registerService(serviceClass, registry);
            }
        }
    }

    private <E extends Entity, S extends Service<E>> void registerService(Class<S> serviceClass,
                                                                          BeanDefinitionRegistry registry) {
        if (ServiceFactory.isServiceClass(serviceClass)) {
            final Class<E> entityClass = getEntityClass(serviceClass);
            if (entityClass != null) {
                final String serviceBeanName = getServiceBeanName(serviceClass);
                if (registry.containsBeanDefinition(serviceBeanName)) {
                    throw new BeanDefinitionValidationException(
                            "service already exist: [" + serviceBeanName + "]" + serviceClass);
                } else {
                    final String collectionBeanName = StringUtils.uncapitalize(entityClass.getSimpleName()) + "Collection";
                    final BeanDefinition definition = buildService(serviceClass, collectionBeanName);
                    LOGER.info("register service[{}] of entity: {} with: {}", serviceBeanName, entityClass, serviceClass);
                    registry.registerBeanDefinition(serviceBeanName, definition);
                }
            }
        }
    }

    private <E extends Entity, S extends Service<E>> Class<E> getEntityClass(Class<S> serviceClass) {
        final Type genericSuperclass;
        if ((genericSuperclass = serviceClass.getGenericSuperclass()) != null) {
            final Class<E> entityClass;
            if ((entityClass = getEntityClassFromGeneric(genericSuperclass)) != null) {
                return entityClass;
            }
        }
        for (Type genericInterface : serviceClass.getGenericInterfaces()) {
            final Class<E> entityClass;
            if ((entityClass = getEntityClassFromGeneric(genericInterface)) != null) {
                return entityClass;
            }
        }
        return null;
    }

    private <E extends Entity> Class<E> getEntityClassFromGeneric(Type genericType) {
        if (genericType instanceof ParameterizedType) {
            for (Type type : ((ParameterizedType) genericType).getActualTypeArguments()) {
                if (Entity.class.isAssignableFrom((Class<?>) type)) {
                    @SuppressWarnings("unchecked") final Class<E> entityClass = (Class<E>) type;
                    if (EntityFactory.isEntityClass(entityClass)) {
                        return entityClass;
                    }
                }
            }
        }
        return null;
    }

    private <E extends Entity, S extends Service<E>> String getServiceBeanName(final Class<S> serviceClass) {
        return StringUtils.uncapitalize(serviceClass.getSimpleName());
    }

    private <E extends Entity, S extends Service<E>> BeanDefinition buildService(Class<S> serviceClass,
                                                                                 String collectionFactoryBeanName) {
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
    public void setBasePackageClasses(Class<?>... basePackageClasses) {
        this.basePackages = new String[basePackageClasses.length];
        for (int i = 0; i < basePackageClasses.length; i++) {
            this.basePackages[i] = basePackageClasses[i].getPackage().getName();
        }
    }

    public void setBasePackages(String... basePackages) {
        this.basePackages = basePackages;
    }

    public void setRecursion(boolean recursion) {
        this.recursion = recursion;
    }
}
