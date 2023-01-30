package test.io.github.dbstarll.dubai.model.spring;


import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.collection.CollectionNameGenerator;
import io.github.dbstarll.dubai.model.spring.autoconfigure.CollectionAutoConfiguration;
import io.github.dbstarll.dubai.model.spring.autoconfigure.MongoAutoConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {MongoAutoConfiguration.class, CollectionAutoConfiguration.class}, webEnvironment = WebEnvironment.NONE)
public class TestCollectionAutoConfiguration implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Test
    public void testGetCollectionNameGenerator() {
        assertNotNull(applicationContext.getBean("collectionNameGenerator", CollectionNameGenerator.class));
    }

    @Test
    public void testGetSimpleEntityCollection() {
        assertNotNull(applicationContext.getBean("simpleEntityCollection", Collection.class));
    }
}
