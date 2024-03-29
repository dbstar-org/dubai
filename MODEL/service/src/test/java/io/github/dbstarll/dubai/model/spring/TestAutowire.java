package io.github.dbstarll.dubai.model.spring;

import com.mongodb.client.MongoDatabase;
import io.github.dbstarll.dubai.model.collection.CollectionFactory;
import io.github.dbstarll.dubai.model.mongodb.MongoClientFactory;
import io.github.dbstarll.dubai.model.service.test3.TestEntity;
import io.github.dbstarll.dubai.model.service.test3.TestService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TestAutowire {
    private ConfigurableApplicationContext context;

    @BeforeEach
    void setUp() {
        final StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.registerSingleton("implementalAutowirer", SpringImplementalAutowirer.class);
        applicationContext.registerBeanDefinition("testAutowire", BeanDefinitionBuilder
                .genericBeanDefinition(TestAutowire.class).getBeanDefinition());
        applicationContext.registerBeanDefinition("mongoDatabase", BeanDefinitionBuilder
                .genericBeanDefinition(MongoDatabase.class)
                .setFactoryMethodOnBean("getDatabase", "testAutowire").getBeanDefinition());
        final CollectionBeanInitializer initializer = new CollectionBeanInitializer();
        initializer.setMongoDatabaseBeanName("mongoDatabase");
        initializer.setBasePackageClasses(TestEntity.class);
        applicationContext.addBeanFactoryPostProcessor(initializer);
        this.context = applicationContext;
    }

    MongoDatabase getDatabase() {
        return new MongoClientFactory().createWithPojoCodec("mongodb://localhost:12345/pumpkin").getDatabase("test");
    }

    @AfterEach
    void tearDown() {
        this.context.close();
        this.context = null;
    }

    /**
     * 测试自动装配.
     */
    @Test
    void testAutowire() {
        final ServiceBeanInitializer initializer = new ServiceBeanInitializer();
        initializer.setBasePackageClasses(TestService.class);
        context.addBeanFactoryPostProcessor(initializer);
        context.refresh();

        assertEquals(6, context.getBeanDefinitionCount());
        assertArrayEquals(new String[]{"implementalAutowirer", "testAutowire", "mongoDatabase",
                        CollectionFactory.class.getName(),
                        "io.github.dbstarll.dubai.model.collection.Collection<io.github.dbstarll.dubai.model.service.test3.TestEntity>",
                        TestService.class.getName()},
                context.getBeanDefinitionNames());

        assertNotNull(context.getBean(TestService.class));
    }
}
