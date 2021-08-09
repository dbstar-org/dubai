package test.io.github.dbstarll.dubai.model.spring;

import io.github.dbstarll.dubai.model.service.test.TestServices;
import io.github.dbstarll.dubai.model.service.test2.InterfaceService;
import io.github.dbstarll.dubai.model.spring.ServiceBeanInitializer;
import junit.framework.TestCase;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.context.support.StaticApplicationContext;

import java.util.Arrays;

public class TestServiceBeanInitializer extends TestCase {
    private BeanDefinitionRegistry registry;

    @Override
    protected void setUp() throws Exception {
        this.registry = new StaticApplicationContext();
    }

    @Override
    protected void tearDown() throws Exception {
        this.registry = null;
    }

    public void testNew() {
        new ServiceBeanInitializer();
    }

    public void testPostProcessBeanFactory() {
        new ServiceBeanInitializer().postProcessBeanFactory(null);
    }

    /**
     * 测试未设置basePackages.
     */
    public void testNullBasePackages() {
        final ServiceBeanInitializer initializer = new ServiceBeanInitializer();
        try {
            initializer.postProcessBeanDefinitionRegistry(registry);
            fail("throw BeanInitializationException");
        } catch (BeansException ex) {
            assertEquals(BeanInitializationException.class, ex.getClass());
            assertEquals("basePackages not set.", ex.getMessage());
        }
    }

    /**
     * 测试未设置basePackages.
     */
    public void testEmptyBasePackages() {
        final ServiceBeanInitializer initializer = new ServiceBeanInitializer();
        initializer.setBasePackages(new String[0]);
        try {
            initializer.postProcessBeanDefinitionRegistry(registry);
            fail("throw BeanInitializationException");
        } catch (BeansException ex) {
            assertEquals(BeanInitializationException.class, ex.getClass());
            assertEquals("basePackages not set.", ex.getMessage());
        }
    }

    /**
     * 测试部分为null的basePackages.
     */
    public void testNullBasePackage() {
        final ServiceBeanInitializer initializer = new ServiceBeanInitializer();
        initializer.setBasePackages(new String[]{"", "abc", null});
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
        final ServiceBeanInitializer initializer = new ServiceBeanInitializer();
        initializer.setBasePackageClasses(TestServices.class);
        initializer.postProcessBeanDefinitionRegistry(registry);
        assertEquals(4, registry.getBeanDefinitionCount());
        assertEquals("[classService, inheritClassService, interfaceService, testEntityService]",
                Arrays.toString(registry.getBeanDefinitionNames()));
    }

    /**
     * 测试递归.
     */
    public void testRecursion() {
        final ServiceBeanInitializer initializer = new ServiceBeanInitializer();
        initializer.setBasePackageClasses(TestServices.class);
        initializer.setRecursion(true);
        initializer.postProcessBeanDefinitionRegistry(registry);
        assertEquals(5, registry.getBeanDefinitionCount());
        assertEquals("[classService, inheritClassService, interfaceService, testEntityService, o2InterfaceService]",
                Arrays.toString(registry.getBeanDefinitionNames()));
    }

    /**
     * 测试有重名的Service.
     */
    public void testSameNameService() {
        final ServiceBeanInitializer initializer = new ServiceBeanInitializer();
        initializer.setBasePackageClasses(TestServices.class, InterfaceService.class);
        try {
            initializer.postProcessBeanDefinitionRegistry(registry);
            fail("throw BeanDefinitionValidationException");
        } catch (BeansException ex) {
            assertEquals(BeanDefinitionValidationException.class, ex.getClass());
            assertEquals("service already exist: [interfaceService]" + InterfaceService.class, ex.getMessage());
        }
    }
}
