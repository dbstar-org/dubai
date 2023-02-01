package test.io.github.dbstarll.dubai.model.spring;


import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.collection.CollectionNameGenerator;
import io.github.dbstarll.dubai.model.spring.autoconfigure.CollectionAutoConfiguration;
import io.github.dbstarll.dubai.model.spring.autoconfigure.DatabaseAutoConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.NONE,
        classes = {
                MongoAutoConfiguration.class,
                DatabaseAutoConfiguration.class,
                CollectionAutoConfiguration.class
        })
public class TestCollectionAutoConfiguration implements ApplicationContextAware {
    private ApplicationContext ctx;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext ctx) throws BeansException {
        this.ctx = ctx;
    }

    @Test
    public void testGetCollectionNameGenerator() {
        assertNotNull(ctx.getBean("collectionNameGenerator", CollectionNameGenerator.class));
    }

    @Test
    public void testGetSimpleEntityCollection() {
        assertNotNull(ctx.getBean("simpleEntityCollection", Collection.class));
    }
}
