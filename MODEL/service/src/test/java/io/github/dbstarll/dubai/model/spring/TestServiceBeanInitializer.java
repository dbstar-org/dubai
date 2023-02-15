package io.github.dbstarll.dubai.model.spring;

import com.mongodb.client.MongoDatabase;
import io.github.dbstarll.dubai.model.entity.test.InterfaceEntity;
import io.github.dbstarll.dubai.model.service.test.TestServices;
import io.github.dbstarll.dubai.model.service.test2.InterfaceService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.context.support.StaticApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class TestServiceBeanInitializer {
    private BeanDefinitionRegistry registry;

    @BeforeEach
    void setUp() {
        this.registry = new StaticApplicationContext();
    }

    @AfterEach
    void tearDown() {
        this.registry = null;
    }

    @Test
    void testNew() {
        try {
            new ServiceBeanInitializer();
        } catch (Exception ex) {
            fail("catch exception");
        }
    }

    @Test
    void testPostProcessBeanFactory() {
        final ServiceBeanInitializer initializer = new ServiceBeanInitializer();
        try {
            initializer.postProcessBeanFactory(new DefaultListableBeanFactory());
        } catch (Exception ex) {
            fail("catch exception");
        }
    }

    /**
     * 测试未设置basePackages.
     */
    @Test
    void testNullBasePackages() {
        final ServiceBeanInitializer initializer = new ServiceBeanInitializer();
        initializer.postProcessBeanDefinitionRegistry(registry);
        assertEquals(0, registry.getBeanDefinitionCount());
    }

    /**
     * 测试未设置basePackages.
     */
    @Test
    void testEmptyBasePackages() {
        final ServiceBeanInitializer initializer = new ServiceBeanInitializer();
        initializer.setBasePackages();
        initializer.postProcessBeanDefinitionRegistry(registry);
        assertEquals(0, registry.getBeanDefinitionCount());
    }

    /**
     * 测试部分为null的basePackages.
     */
    @Test
    void testNullBasePackage() {
        final ServiceBeanInitializer initializer = new ServiceBeanInitializer();
        initializer.setBasePackages("", "abc", null);
        try {
            initializer.postProcessBeanDefinitionRegistry(registry);
            fail("throw BeanDefinitionStoreException");
        } catch (BeansException ex) {
            assertEquals(BeanDefinitionStoreException.class, ex.getClass());
            assertEquals(IllegalArgumentException.class, ex.getCause().getClass());
            assertEquals("Class name must not be null", ex.getCause().getMessage());
        }
    }

    /**
     * 测试通过class方式来设置basePackages.
     */
    @Test
    void testBasePackageClasses() {
        registry.registerBeanDefinition("mongoDatabase",
                BeanDefinitionBuilder.rootBeanDefinition(MongoDatabase.class).getBeanDefinition());
        final CollectionBeanInitializer collectionBeanInitializer = new CollectionBeanInitializer();
        collectionBeanInitializer.setBasePackageClasses(InterfaceEntity.class);
        collectionBeanInitializer.setMongoDatabaseBeanName("mongoDatabase");
        collectionBeanInitializer.postProcessBeanDefinitionRegistry(registry);
        final ServiceBeanInitializer initializer = new ServiceBeanInitializer();
        initializer.setBasePackageClasses(TestServices.class);
        initializer.postProcessBeanDefinitionRegistry(registry);
        assertEquals(10, registry.getBeanDefinitionCount());
    }

    /**
     * 测试递归.
     */
    @Test
    void testRecursion() {
        registry.registerBeanDefinition("mongoDatabase",
                BeanDefinitionBuilder.rootBeanDefinition(MongoDatabase.class).getBeanDefinition());
        final CollectionBeanInitializer collectionBeanInitializer = new CollectionBeanInitializer();
        collectionBeanInitializer.setBasePackageClasses(InterfaceEntity.class);
        collectionBeanInitializer.setMongoDatabaseBeanName("mongoDatabase");
        collectionBeanInitializer.postProcessBeanDefinitionRegistry(registry);
        final ServiceBeanInitializer initializer = new ServiceBeanInitializer();
        initializer.setBasePackageClasses(TestServices.class);
        initializer.setRecursion(true);
        initializer.postProcessBeanDefinitionRegistry(registry);
        assertEquals(11, registry.getBeanDefinitionCount());
    }

    /**
     * 测试有重名的Service.
     */
    @Test
    void testSameNameService() {
        registry.registerBeanDefinition("mongoDatabase",
                BeanDefinitionBuilder.rootBeanDefinition(MongoDatabase.class).getBeanDefinition());
        final CollectionBeanInitializer collectionBeanInitializer = new CollectionBeanInitializer();
        collectionBeanInitializer.setBasePackageClasses(InterfaceEntity.class);
        collectionBeanInitializer.setMongoDatabaseBeanName("mongoDatabase");
        collectionBeanInitializer.postProcessBeanDefinitionRegistry(registry);
        final ServiceBeanInitializer initializer = new ServiceBeanInitializer();
        initializer.setBasePackageClasses(TestServices.class, InterfaceService.class);
        initializer.postProcessBeanDefinitionRegistry(registry);
        assertEquals(11, registry.getBeanDefinitionCount());
    }

    /**
     * 测试有重复的Service.
     */
    @Test
    void testDuplicateService() {
        registry.registerBeanDefinition("mongoDatabase",
                BeanDefinitionBuilder.rootBeanDefinition(MongoDatabase.class).getBeanDefinition());

        final CollectionBeanInitializer collectionBeanInitializer = new CollectionBeanInitializer();
        collectionBeanInitializer.setBasePackageClasses(InterfaceEntity.class);
        collectionBeanInitializer.setMongoDatabaseBeanName("mongoDatabase");
        collectionBeanInitializer.postProcessBeanDefinitionRegistry(registry);

        final ServiceBeanInitializer initializer = new ServiceBeanInitializer();
        initializer.setBasePackageClasses(TestServices.class);
        initializer.postProcessBeanDefinitionRegistry(registry);
        assertEquals(10, registry.getBeanDefinitionCount());

        try {
            initializer.postProcessBeanDefinitionRegistry(registry);
        } catch (BeansException ex) {
            assertEquals(BeanDefinitionValidationException.class, ex.getClass());
            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().startsWith("service already exist: "));
        }
    }

    /**
     * 测试有抛出BeansException.
     */
    @Test
    void testBeansException() {
        final BeanDefinitionRegistry testRegistry = new SimpleBeanDefinitionRegistry() {
            @Override
            public boolean containsBeanDefinition(String beanName) {
                throw new NoSuchBeanDefinitionException("no");
            }
        };

        final ServiceBeanInitializer initializer = new ServiceBeanInitializer();
        initializer.setBasePackageClasses(TestServices.class);

        try {
            initializer.postProcessBeanDefinitionRegistry(testRegistry);
        } catch (BeansException ex) {
            assertEquals(NoSuchBeanDefinitionException.class, ex.getClass());
            assertEquals("No bean named 'no' available", ex.getMessage());
        }
    }
}
