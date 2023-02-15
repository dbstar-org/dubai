package io.github.dbstarll.dubai.model.spring;

import com.mongodb.client.MongoDatabase;
import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.collection.CollectionFactory;
import io.github.dbstarll.dubai.model.collection.test.o2.SimpleEntity;
import io.github.dbstarll.dubai.model.entity.Entity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.core.ResolvableType;
import org.springframework.lang.NonNull;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class TestCollectionBeanInitializer {
    private static final String COLLECTION_FACTORY_BEAN_NAME = CollectionFactory.class.getName();
    private static final String MONGO_DATABASE_BEAN_NAME = "mongoDatabase";

    private CollectionBeanInitializer initializer;

    private BeanDefinitionRegistry beanDefinitionRegistry;

    /**
     * 初始化MongoCollectionBeanInitializer.
     */
    @BeforeEach
    void setup() {
        this.initializer = new CollectionBeanInitializer();
        initializer.setMongoDatabaseBeanName(MONGO_DATABASE_BEAN_NAME);
        this.beanDefinitionRegistry = new SimpleBeanDefinitionRegistry();
        beanDefinitionRegistry.registerBeanDefinition(MONGO_DATABASE_BEAN_NAME,
                BeanDefinitionBuilder.rootBeanDefinition(MongoDatabase.class).getBeanDefinition());
    }

    @AfterEach
    void clean() {
        this.initializer = null;
        this.beanDefinitionRegistry = null;
    }

    private <E extends Entity> String getCollectionBeanName(final Class<E> entityClass) {
        return ResolvableType.forClassWithGenerics(Collection.class, entityClass).toString();
    }

    @Test
    void testPostProcessBeanFactory() {
        try {
            initializer.postProcessBeanFactory(new DefaultListableBeanFactory());
        } catch (BeansException ex) {
            fail("throws BeansException");
        }
    }

    @Test
    void testCollectionFactory() {
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
        registry.registerBeanDefinition(MONGO_DATABASE_BEAN_NAME,
                BeanDefinitionBuilder.rootBeanDefinition(MongoDatabase.class).getBeanDefinition());

        assertEquals(0, containsCounter.get());
        assertEquals(0, registerCounter.get());

        initializer.setBasePackages();
        initializer.postProcessBeanDefinitionRegistry(registry);
        assertEquals(1, containsCounter.get());
        assertEquals(1, registerCounter.get());
        assertEquals(2, registry.getBeanDefinitionCount());

        initializer.postProcessBeanDefinitionRegistry(registry);
        assertEquals(2, containsCounter.get());
        assertEquals(1, registerCounter.get());
        assertEquals(2, registry.getBeanDefinitionCount());
    }

    /**
     * 测试未设置basePackages.
     */
    @Test
    void testEmptyBasePackages() {
        initializer.setBasePackages();
        initializer.postProcessBeanDefinitionRegistry(beanDefinitionRegistry);
        assertEquals(2, beanDefinitionRegistry.getBeanDefinitionCount());
    }

    /**
     * 测试未设置basePackages.
     */
    @Test
    void testNullBasePackages() {
        initializer.postProcessBeanDefinitionRegistry(beanDefinitionRegistry);
        assertEquals(2, beanDefinitionRegistry.getBeanDefinitionCount());
    }

    @Test
    void testNullMongoDatabaseBeanName() {
        initializer.setMongoDatabaseBeanName(null);
        try {
            initializer.postProcessBeanDefinitionRegistry(beanDefinitionRegistry);
            fail("throw BeanDefinitionValidationException");
        } catch (BeanDefinitionValidationException ex) {
            assertEquals("mongoDatabaseBeanName not set.", ex.getMessage());
        }
    }

    @Test
    void testEmptyMongoDatabaseBeanName() {
        initializer.setMongoDatabaseBeanName("");
        try {
            initializer.postProcessBeanDefinitionRegistry(beanDefinitionRegistry);
            fail("throw BeanDefinitionValidationException");
        } catch (BeanDefinitionValidationException ex) {
            assertEquals("mongoDatabaseBeanName not set.", ex.getMessage());
        }
    }

    @Test
    void testOtherMongoDatabaseBeanName() {
        initializer.setMongoDatabaseBeanName("other");
        try {
            initializer.postProcessBeanDefinitionRegistry(beanDefinitionRegistry);
            fail("throw NoSuchBeanDefinitionException");
        } catch (NoSuchBeanDefinitionException ex) {
            assertEquals("No bean named 'other' available", ex.getMessage());
        }
    }

    @Test
    void testNoRecursion() {
        initializer.setBasePackages("io.github.dbstarll.dubai.model.collection.test.o2");
        initializer.postProcessBeanDefinitionRegistry(beanDefinitionRegistry);

        assertEquals(7, beanDefinitionRegistry.getBeanDefinitionCount());
        assertTrue(beanDefinitionRegistry.containsBeanDefinition(COLLECTION_FACTORY_BEAN_NAME));
        assertTrue(beanDefinitionRegistry.containsBeanDefinition(getCollectionBeanName(SimpleEntity.class)));
    }

    @Test
    void testRecursion() {
        initializer.setBasePackages("io.github.dbstarll.dubai.model.collection.test");
        initializer.setRecursion(true);
        initializer.postProcessBeanDefinitionRegistry(beanDefinitionRegistry);

        assertEquals(21, beanDefinitionRegistry.getBeanDefinitionCount());
        assertTrue(beanDefinitionRegistry.containsBeanDefinition(getCollectionBeanName(SimpleEntity.class)));
        assertTrue(beanDefinitionRegistry.containsBeanDefinition(getCollectionBeanName(io.github.dbstarll.dubai.model.collection.test.SimpleEntity.class)));
    }

    @Test
    void testIsCollectionBeanDefinition() {
        initializer.setBasePackages("io.github.dbstarll.dubai.model.collection.test.o2");
        initializer.postProcessBeanDefinitionRegistry(beanDefinitionRegistry);

        assertEquals(7, beanDefinitionRegistry.getBeanDefinitionCount());
        final BeanDefinition db = beanDefinitionRegistry.getBeanDefinition("mongoDatabase");
        assertFalse(CollectionBeanInitializer.isCollectionBeanDefinition(db, SimpleEntity.class));

        final BeanDefinition df = beanDefinitionRegistry.getBeanDefinition(getCollectionBeanName(SimpleEntity.class));
        assertNotNull(df);
        assertTrue(CollectionBeanInitializer.isCollectionBeanDefinition(df, SimpleEntity.class));
        assertFalse(CollectionBeanInitializer.isCollectionBeanDefinition(df,
                io.github.dbstarll.dubai.model.collection.test.SimpleEntity.class));

        final BeanDefinition df2 = BeanDefinitionBuilder.genericBeanDefinition(Collection.class)
                .setFactoryMethodOnBean("newInstance", "collectionFactory")
                .setScope(BeanDefinition.SCOPE_SINGLETON)
                .setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE)
                .getBeanDefinition();
        assertFalse(CollectionBeanInitializer.isCollectionBeanDefinition(df2, SimpleEntity.class));
    }

    @Test
    void testExistCollection() {
        initializer.setBasePackageClasses(SimpleEntity.class);
        initializer.postProcessBeanDefinitionRegistry(beanDefinitionRegistry);

        assertEquals(7, beanDefinitionRegistry.getBeanDefinitionCount());

        try {
            initializer.postProcessBeanDefinitionRegistry(beanDefinitionRegistry);
            fail("throw BeanDefinitionValidationException");
        } catch (BeanDefinitionValidationException ex) {
            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().startsWith("collection already exist: "));
        }
    }

    @Test
    void testException() {
        final BeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry() {
            @Override
            public void registerBeanDefinition(@NonNull String beanName, BeanDefinition beanDefinition)
                    throws BeanDefinitionStoreException {
                if (beanName.startsWith(Collection.class.getName() + "<")) {
                    throw new IllegalArgumentException("TestIOException");
                }
                super.registerBeanDefinition(beanName, beanDefinition);
            }
        };
        registry.registerBeanDefinition(MONGO_DATABASE_BEAN_NAME,
                BeanDefinitionBuilder.rootBeanDefinition(MongoDatabase.class).getBeanDefinition());

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
