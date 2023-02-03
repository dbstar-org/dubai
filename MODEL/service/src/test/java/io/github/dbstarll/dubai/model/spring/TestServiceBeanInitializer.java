package io.github.dbstarll.dubai.model.spring;

import io.github.dbstarll.dubai.model.entity.test.InterfaceEntity;
import io.github.dbstarll.dubai.model.service.test.TestServices;
import io.github.dbstarll.dubai.model.service.test2.InterfaceService;
import junit.framework.TestCase;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.StaticApplicationContext;

public class TestServiceBeanInitializer extends TestCase {
    private BeanDefinitionRegistry registry;

    @Override
    protected void setUp() {
        this.registry = new StaticApplicationContext();
    }

    @Override
    protected void tearDown() {
        this.registry = null;
    }

    public void testNew() {
        try {
            new ServiceBeanInitializer();
        } catch (Exception ex) {
            fail("catch exception");
        }
    }

    public void testPostProcessBeanFactory() {
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
    public void testNullBasePackages() {
        final ServiceBeanInitializer initializer = new ServiceBeanInitializer();
        initializer.postProcessBeanDefinitionRegistry(registry);
        assertEquals(0, registry.getBeanDefinitionCount());
    }

    /**
     * 测试未设置basePackages.
     */
    public void testEmptyBasePackages() {
        final ServiceBeanInitializer initializer = new ServiceBeanInitializer();
        initializer.setBasePackages();
        initializer.postProcessBeanDefinitionRegistry(registry);
        assertEquals(0, registry.getBeanDefinitionCount());
    }

    /**
     * 测试部分为null的basePackages.
     */
    public void testNullBasePackage() {
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
    public void testBasePackageClasses() {
        final CollectionBeanInitializer collectionBeanInitializer = new CollectionBeanInitializer();
        collectionBeanInitializer.setBasePackageClasses(InterfaceEntity.class);
        collectionBeanInitializer.setMongoDatabaseBeanName("mongoDatabase");
        collectionBeanInitializer.postProcessBeanDefinitionRegistry(registry);
        final ServiceBeanInitializer initializer = new ServiceBeanInitializer();
        initializer.setBasePackageClasses(TestServices.class);
        initializer.postProcessBeanDefinitionRegistry(registry);
        assertEquals(9, registry.getBeanDefinitionCount());
    }

    /**
     * 测试递归.
     */
    public void testRecursion() {
        final CollectionBeanInitializer collectionBeanInitializer = new CollectionBeanInitializer();
        collectionBeanInitializer.setBasePackageClasses(InterfaceEntity.class);
        collectionBeanInitializer.setMongoDatabaseBeanName("mongoDatabase");
        collectionBeanInitializer.postProcessBeanDefinitionRegistry(registry);
        final ServiceBeanInitializer initializer = new ServiceBeanInitializer();
        initializer.setBasePackageClasses(TestServices.class);
        initializer.setRecursion(true);
        initializer.postProcessBeanDefinitionRegistry(registry);
        assertEquals(10, registry.getBeanDefinitionCount());
    }

    /**
     * 测试有重名的Service.
     */
    public void testSameNameService() {
        final CollectionBeanInitializer collectionBeanInitializer = new CollectionBeanInitializer();
        collectionBeanInitializer.setBasePackageClasses(InterfaceEntity.class);
        collectionBeanInitializer.setMongoDatabaseBeanName("mongoDatabase");
        collectionBeanInitializer.postProcessBeanDefinitionRegistry(registry);
        final ServiceBeanInitializer initializer = new ServiceBeanInitializer();
        initializer.setBasePackageClasses(TestServices.class, InterfaceService.class);
        initializer.postProcessBeanDefinitionRegistry(registry);
        assertEquals(10, registry.getBeanDefinitionCount());
    }
}
