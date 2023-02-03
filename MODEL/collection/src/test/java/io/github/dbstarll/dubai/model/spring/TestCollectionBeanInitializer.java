package io.github.dbstarll.dubai.model.spring;

import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.collection.test.o2.SimpleEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.lang.NonNull;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestCollectionBeanInitializer {
    private static final String COLLECTION_FACTORY_BEAN_NAME = "collectionFactory";
    private static final String MONGO_DATABASE_BEAN_NAME = "mongoDatabase";

    private CollectionBeanInitializer initializer;

    private BeanDefinitionRegistry beanDefinitionRegistry;

    /**
     * 初始化MongoCollectionBeanInitializer.
     */
    @Before
    public void setup() {
        this.initializer = new CollectionBeanInitializer();
        initializer.setCollectionFactoryBeanName(COLLECTION_FACTORY_BEAN_NAME);
        initializer.setMongoDatabaseBeanName(MONGO_DATABASE_BEAN_NAME);
        this.beanDefinitionRegistry = new SimpleBeanDefinitionRegistry();
    }

    @After
    public void clean() {
        this.initializer = null;
        this.beanDefinitionRegistry = null;
    }

    @Test
    public void testPostProcessBeanFactory() {
        try {
            initializer.postProcessBeanFactory(new DefaultListableBeanFactory());
        } catch (BeansException ex) {
            fail("throws BeansException");
        }
    }

    @Test
    public void testCollectionFactory() {
        final AtomicInteger containsCounter = new AtomicInteger();
        final AtomicInteger registerCounter = new AtomicInteger();

        final BeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry() {
            @Override
            public boolean containsBeanDefinition(@NonNull String beanName) {
                if (COLLECTION_FACTORY_BEAN_NAME.equals(beanName)) {
                    containsCounter.incrementAndGet();
                }
                return super.containsBeanDefinition(beanName);
            }

            @Override
            public void registerBeanDefinition(@NonNull String beanName, BeanDefinition beanDefinition) throws BeanDefinitionStoreException {
                super.registerBeanDefinition(beanName, beanDefinition);
                if (COLLECTION_FACTORY_BEAN_NAME.equals(beanName)) {
                    registerCounter.incrementAndGet();
                }
            }
        };

        assertEquals(0, containsCounter.get());
        assertEquals(0, registerCounter.get());

        initializer.setBasePackages();
        initializer.postProcessBeanDefinitionRegistry(registry);
        assertEquals(1, containsCounter.get());
        assertEquals(1, registerCounter.get());
        assertEquals(1, registry.getBeanDefinitionCount());

        initializer.postProcessBeanDefinitionRegistry(registry);
        assertEquals(2, containsCounter.get());
        assertEquals(1, registerCounter.get());
        assertEquals(1, registry.getBeanDefinitionCount());
    }

    /**
     * 测试未设置basePackages.
     */
    @Test
    public void testEmptyBasePackages() {
        initializer.setBasePackages();
        initializer.postProcessBeanDefinitionRegistry(beanDefinitionRegistry);
        assertEquals(1, beanDefinitionRegistry.getBeanDefinitionCount());
    }

    /**
     * 测试未设置basePackages.
     */
    @Test
    public void testNullBasePackages() {
        initializer.postProcessBeanDefinitionRegistry(beanDefinitionRegistry);
        assertEquals(1, beanDefinitionRegistry.getBeanDefinitionCount());
    }

    @Test
    public void testNoRecursion() {
        initializer.setBasePackages("io.github.dbstarll.dubai.model.collection.test.o2");
        initializer.postProcessBeanDefinitionRegistry(beanDefinitionRegistry);

        assertEquals(6, beanDefinitionRegistry.getBeanDefinitionCount());
        assertTrue(beanDefinitionRegistry.containsBeanDefinition(COLLECTION_FACTORY_BEAN_NAME));
        assertTrue(beanDefinitionRegistry.containsBeanDefinition("simpleEntityCollection"));
    }

    @Test
    public void testRecursion() {
        initializer.setBasePackages("io.github.dbstarll.dubai.model.collection.test");
        initializer.setRecursion(true);
        initializer.postProcessBeanDefinitionRegistry(beanDefinitionRegistry);

        assertEquals(20, beanDefinitionRegistry.getBeanDefinitionCount());
        assertTrue(beanDefinitionRegistry.containsBeanDefinition("simpleEntityCollection"));
        assertTrue(beanDefinitionRegistry.containsBeanDefinition("simpleEntityCollection2"));
    }

    @Test
    public void testIsCollectionBeanDefinition() {
        initializer.setBasePackages("io.github.dbstarll.dubai.model.collection.test.o2");
        initializer.postProcessBeanDefinitionRegistry(beanDefinitionRegistry);

        assertEquals(6, beanDefinitionRegistry.getBeanDefinitionCount());
        final BeanDefinition df = beanDefinitionRegistry.getBeanDefinition("simpleEntityCollection");
        assertNotNull(df);
        assertTrue(CollectionBeanInitializer.isCollectionBeanDefinition(df, SimpleEntity.class));
        assertFalse(CollectionBeanInitializer.isCollectionBeanDefinition(df,
                io.github.dbstarll.dubai.model.collection.test.SimpleEntity.class));

        final BeanDefinition df2 = BeanDefinitionBuilder.genericBeanDefinition(Collection.class)
                .setFactoryMethodOnBean("newInstance", "collectionFactory")
                .setScope(BeanDefinition.SCOPE_SINGLETON)
                .setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_NAME)
                .getBeanDefinition();
        assertFalse(CollectionBeanInitializer.isCollectionBeanDefinition(df2, SimpleEntity.class));
    }

    @Test
    public void testExistCollection() {
        initializer.setBasePackageClasses(SimpleEntity.class);
        initializer.postProcessBeanDefinitionRegistry(beanDefinitionRegistry);

        assertEquals(6, beanDefinitionRegistry.getBeanDefinitionCount());

        try {
            initializer.postProcessBeanDefinitionRegistry(beanDefinitionRegistry);
            fail("throw BeanDefinitionValidationException");
        } catch (BeanDefinitionValidationException ex) {
            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().startsWith("collection already exist: "));
        }
    }

    @Test
    public void testException() {
        final BeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry() {
            @Override
            public void registerBeanDefinition(@NonNull String beanName, BeanDefinition beanDefinition)
                    throws BeanDefinitionStoreException {
                if ("simpleEntityCollection".equals(beanName)) {
                    throw new IllegalArgumentException("TestIOException");
                }
                super.registerBeanDefinition(beanName, beanDefinition);
            }
        };

        initializer.setBasePackageClasses(SimpleEntity.class);
        try {
            initializer.postProcessBeanDefinitionRegistry(registry);
            fail("throw BeanDefinitionStoreException");
        } catch (BeanDefinitionStoreException ex) {
            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().startsWith("I/O failure during classpath scanning"));
            assertEquals(IllegalArgumentException.class, ex.getCause().getClass());
            assertEquals("TestIOException", ex.getCause().getMessage());
        }
    }
}
