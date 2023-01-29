package test.io.github.dbstarll.dubai.model.spring;

import io.github.dbstarll.dubai.model.collection.test.o2.SimpleEntity;
import io.github.dbstarll.dubai.model.spring.MongoCollectionBeanInitializer;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestMongoCollectionBeanInitializer {
    private static final String COLLECTION_FACTORY_BEAN_NAME = "collectionFactory";
    private static final String MONGO_DATABASE_BEAN_NAME = "mongoDatabase";

    @Mocked
    ConfigurableListableBeanFactory configurableListableBeanFactory;

    @Mocked
    BeanDefinitionRegistry beanDefinitionRegistry;

    MongoCollectionBeanInitializer initializer;

    /**
     * 初始化MongoCollectionBeanInitializer.
     */
    @Before
    public void initialize() {
        this.initializer = new MongoCollectionBeanInitializer();
        initializer.setCollectionFactoryBeanName(COLLECTION_FACTORY_BEAN_NAME);
        initializer.setMongoDatabaseBeanName(MONGO_DATABASE_BEAN_NAME);
    }

    @Test
    public void testPostProcessBeanFactory() {
        try {
            initializer.postProcessBeanFactory(configurableListableBeanFactory);
        } catch (Exception ex) {
            fail("catch Exception");
        }
    }

    @Test
    public void testMissCollectionFactory() {
        new Expectations() {
            {
                beanDefinitionRegistry.containsBeanDefinition(COLLECTION_FACTORY_BEAN_NAME);
                result = false;
            }
        };

        initializer.setBasePackages();
        initializer.postProcessBeanDefinitionRegistry(beanDefinitionRegistry);

        new Verifications() {
            {
                beanDefinitionRegistry.containsBeanDefinition(COLLECTION_FACTORY_BEAN_NAME);
                times = 1;
                beanDefinitionRegistry.registerBeanDefinition(COLLECTION_FACTORY_BEAN_NAME, (BeanDefinition) any);
                times = 1;
            }
        };
    }

    @Test
    public void testContainsCollectionFactory() {
        new Expectations() {
            {
                beanDefinitionRegistry.containsBeanDefinition(COLLECTION_FACTORY_BEAN_NAME);
                result = true;
            }
        };

        initializer.setBasePackages();
        initializer.postProcessBeanDefinitionRegistry(beanDefinitionRegistry);

        new Verifications() {
            {
                beanDefinitionRegistry.containsBeanDefinition(COLLECTION_FACTORY_BEAN_NAME);
                times = 1;
                beanDefinitionRegistry.registerBeanDefinition(COLLECTION_FACTORY_BEAN_NAME, (BeanDefinition) any);
                times = 0;
            }
        };
    }

    @Test
    public void testNoRecursion() {
        initializer.setBasePackages("io.github.dbstarll.dubai.model.collection.test.o2");
        initializer.postProcessBeanDefinitionRegistry(beanDefinitionRegistry);

        new Verifications() {
            {
                beanDefinitionRegistry.containsBeanDefinition(COLLECTION_FACTORY_BEAN_NAME);
                times = 1;
                beanDefinitionRegistry.registerBeanDefinition(COLLECTION_FACTORY_BEAN_NAME, (BeanDefinition) any);
                times = 1;
                beanDefinitionRegistry.containsBeanDefinition("simpleEntityCollection");
                times = 1;
                beanDefinitionRegistry.registerBeanDefinition("simpleEntityCollection", (BeanDefinition) any);
                times = 1;
            }
        };
    }

    @Test
    public void testRecursion() {
        initializer.setBasePackages("io.github.dbstarll.dubai.model.collection.test");
        initializer.setRecursion(true);
        initializer.postProcessBeanDefinitionRegistry(beanDefinitionRegistry);

        new Verifications() {
            {
                beanDefinitionRegistry.containsBeanDefinition(COLLECTION_FACTORY_BEAN_NAME);
                times = 1;
                beanDefinitionRegistry.registerBeanDefinition(COLLECTION_FACTORY_BEAN_NAME, (BeanDefinition) any);
                times = 1;
                beanDefinitionRegistry.containsBeanDefinition("simpleEntityCollection");
                times = 2;
                beanDefinitionRegistry.registerBeanDefinition("simpleEntityCollection", (BeanDefinition) any);
                times = 2;
            }
        };
    }

    @Test
    public void testExistCollection() {
        new Expectations() {
            {
                beanDefinitionRegistry.containsBeanDefinition("simpleEntityCollection");
                result = true;
            }
        };

        initializer.setBasePackageClasses(SimpleEntity.class);
        try {
            initializer.postProcessBeanDefinitionRegistry(beanDefinitionRegistry);
            fail("throw BeanDefinitionValidationException");
        } catch (BeanDefinitionValidationException ex) {
            assertTrue(ex.getMessage().startsWith("collection already exist: "));
        }

        new Verifications() {
            {
                beanDefinitionRegistry.containsBeanDefinition(COLLECTION_FACTORY_BEAN_NAME);
                times = 1;
                beanDefinitionRegistry.registerBeanDefinition(COLLECTION_FACTORY_BEAN_NAME, (BeanDefinition) any);
                times = 1;
                beanDefinitionRegistry.containsBeanDefinition("simpleEntityCollection");
                times = 1;
                beanDefinitionRegistry.registerBeanDefinition("simpleEntityCollection", (BeanDefinition) any);
                times = 0;
            }
        };
    }

    @Test
    public void testException() {
        new Expectations() {
            {
                beanDefinitionRegistry.registerBeanDefinition("simpleEntityCollection", (BeanDefinition) any);
                result = new IOException("TestIOException");
            }
        };

        initializer.setBasePackageClasses(SimpleEntity.class);
        try {
            initializer.postProcessBeanDefinitionRegistry(beanDefinitionRegistry);
            fail("throw BeanDefinitionStoreException");
        } catch (BeanDefinitionStoreException ex) {
            assertTrue(ex.getMessage().startsWith("I/O failure during classpath scanning"));
            assertEquals(IOException.class, ex.getCause().getClass());
            assertEquals("TestIOException", ex.getCause().getMessage());
        }

        new Verifications() {
            {
                beanDefinitionRegistry.containsBeanDefinition(COLLECTION_FACTORY_BEAN_NAME);
                times = 1;
                beanDefinitionRegistry.registerBeanDefinition(COLLECTION_FACTORY_BEAN_NAME, (BeanDefinition) any);
                times = 1;
                beanDefinitionRegistry.containsBeanDefinition("simpleEntityCollection");
                times = 1;
                beanDefinitionRegistry.registerBeanDefinition("simpleEntityCollection", (BeanDefinition) any);
                times = 1;
            }
        };
    }
}
