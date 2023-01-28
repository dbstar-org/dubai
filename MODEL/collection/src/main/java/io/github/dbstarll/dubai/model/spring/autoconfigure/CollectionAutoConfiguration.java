package io.github.dbstarll.dubai.model.spring.autoconfigure;

import io.github.dbstarll.dubai.model.collection.AnnotationCollectionNameGenerator;
import io.github.dbstarll.dubai.model.collection.CollectionNameGenerator;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.spring.MongoCollectionBeanInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.SpringFactoriesLoader;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class CollectionAutoConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(MongoCollectionBeanInitializer.class)
    static BeanDefinitionRegistryPostProcessor mongoCollectionBeanInitializer() {
        MongoCollectionBeanInitializer initializer = new MongoCollectionBeanInitializer();
        initializer
                .setBasePackageClasses(loadBasePackages(Entity.class, CollectionAutoConfiguration.class.getClassLoader()));
        initializer.setMongoDatabaseBeanName("mongoDatabase");
        return initializer;
    }

    @Bean
    @ConditionalOnMissingBean(CollectionNameGenerator.class)
    CollectionNameGenerator collectionNameGenerator() {
        return new AnnotationCollectionNameGenerator();
    }

    /**
     * 加载BasePackage列表.
     *
     * @param baseClass   baseClass
     * @param classLoader classLoader
     * @return BasePackage列表
     */
    public static Class<?>[] loadBasePackages(Class<?> baseClass, ClassLoader classLoader) {
        final List<Class<?>> packages = new ArrayList<>();
        for (String packageClassName : SpringFactoriesLoader.loadFactoryNames(baseClass, classLoader)) {
            final Class<?> packageClass;
            try {
                packageClass = classLoader.loadClass(packageClassName);
            } catch (ClassNotFoundException e) {
                LOGGER.warn("loadClass failed: " + packageClassName, e);
                continue;
            }

            if (!baseClass.isAssignableFrom(packageClass)) {
                LOGGER.warn("{} not extends from {}", packageClass, baseClass);
            } else {
                packages.add(packageClass);
            }
        }
        return packages.toArray(new Class<?>[0]);
    }
}
