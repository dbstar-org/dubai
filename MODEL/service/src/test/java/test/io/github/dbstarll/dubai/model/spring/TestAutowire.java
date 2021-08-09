package test.io.github.dbstarll.dubai.model.spring;

import com.mongodb.client.MongoDatabase;
import io.github.dbstarll.dubai.model.mongodb.MongoClientFactory;
import io.github.dbstarll.dubai.model.service.test3.TestEntity;
import io.github.dbstarll.dubai.model.service.test3.TestService;
import io.github.dbstarll.dubai.model.spring.MongoCollectionBeanInitializer;
import io.github.dbstarll.dubai.model.spring.ServiceBeanInitializer;
import io.github.dbstarll.dubai.model.spring.SpringImplementalAutowirer;
import junit.framework.TestCase;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

import java.util.Arrays;

public class TestAutowire extends TestCase {
    private ConfigurableApplicationContext context;

    @Override
    protected void setUp() throws Exception {
        final StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.registerSingleton("implementalAutowirer", SpringImplementalAutowirer.class);
        applicationContext.registerBeanDefinition("mongoDatabase", BeanDefinitionBuilder
                .genericBeanDefinition(TestAutowire.class).setFactoryMethod("getDatabase").getBeanDefinition());
        final MongoCollectionBeanInitializer initializer = new MongoCollectionBeanInitializer();
        initializer.setMongoDatabaseBeanName("mongoDatabase");
        initializer.setBasePackageClasses(TestEntity.class);
        applicationContext.addBeanFactoryPostProcessor(initializer);
        this.context = applicationContext;
    }

    static MongoDatabase getDatabase() throws Exception {
        return new MongoClientFactory().createWithPojoCodec("mongodb://localhost:12345/pumpkin").getDatabase("test");
    }

    @Override
    protected void tearDown() throws Exception {
        this.context.close();
        this.context = null;
    }

    /**
     * 测试自动装配.
     */
    public void testAutowire() {
        final ServiceBeanInitializer initializer = new ServiceBeanInitializer();
        initializer.setBasePackageClasses(TestService.class);
        context.addBeanFactoryPostProcessor(initializer);
        context.refresh();

        assertEquals(5, context.getBeanDefinitionCount());
        assertTrue(Arrays.equals(new String[]{"implementalAutowirer", "mongoDatabase", "collectionFactory",
                "testEntityCollection", "testService"}, context.getBeanDefinitionNames()));

        assertNotNull(context.getBean(TestService.class));
    }
}
