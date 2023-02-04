package io.github.dbstarll.dubai.model.spring.autoconfigure;

import com.mongodb.client.MongoDatabase;
import io.github.dbstarll.dubai.model.collection.AnnotationCollectionNameGenerator;
import io.github.dbstarll.dubai.model.collection.CollectionNameGenerator;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.spring.CollectionBeanInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.support.SpringFactoriesLoader;

import java.util.ArrayList;
import java.util.List;

@AutoConfiguration
@AutoConfigureAfter(DatabaseAutoConfiguration.class)
public class CollectionAutoConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionAutoConfiguration.class);

    /**
     * 遍历所有Entity并为其装配CollectionBean，然后注入到Spring Context中.
     *
     * @return MongoCollectionBeanInitializer实例.
     */
    @Bean
    @ConditionalOnBean(name = "mongoDatabase", value = MongoDatabase.class)
    @ConditionalOnMissingBean(CollectionBeanInitializer.class)
    static BeanDefinitionRegistryPostProcessor mongoCollectionBeanInitializer() {
        final CollectionBeanInitializer initializer = new CollectionBeanInitializer();
        initializer.setBasePackageClasses(loadBasePackages(Entity.class, Entity.class.getClassLoader()));
        initializer.setMongoDatabaseBeanName("mongoDatabase");
        return initializer;
    }

    /**
     * 注入CollectionNameGenerator实例，用于为Collection生成Bean的名字.
     *
     * @return CollectionNameGenerator实例
     */
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
    public static Class<?>[] loadBasePackages(final Class<?> baseClass, final ClassLoader classLoader) {
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
