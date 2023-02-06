package io.github.dbstarll.dubai.model.spring;


import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.collection.CollectionNameGenerator;
import io.github.dbstarll.dubai.model.collection.test.SimpleEntity;
import io.github.dbstarll.dubai.model.spring.autoconfigure.CollectionAutoConfiguration;
import io.github.dbstarll.dubai.model.spring.autoconfigure.DatabaseAutoConfiguration;
import org.assertj.core.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.ResolvableType;
import org.springframework.lang.NonNull;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.NONE,
        classes = {
                MongoAutoConfiguration.class,
                DatabaseAutoConfiguration.class,
                CollectionAutoConfiguration.class
        })
public class TestCollectionAutoConfiguration implements ApplicationContextAware {
    private ApplicationContext ctx;

    @Autowired(required = false)
    private Collection<SimpleEntity> collection1;

    @Autowired(required = false)
    private Collection<io.github.dbstarll.dubai.model.collection.test.o2.SimpleEntity> collection2;

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
        assertNotNull(collection1);
        assertSame(SimpleEntity.class, collection1.getEntityClass());
    }

    @Test
    public void testGetSimpleEntityCollection2() {
        assertNull(collection2);
    }

    @Test
    public void testGetSimpleEntityCollectionByType() {
        final ResolvableType beanType = ResolvableType.forClassWithGenerics(Collection.class, SimpleEntity.class);
        assertArrayEquals(Arrays.array(beanType.toString()), ctx.getBeanNamesForType(beanType));
    }

    @Test
    public void testGetSimpleEntityCollectionByType2() {
        assertEquals(0,
                ctx.getBeanNamesForType(ResolvableType.forClassWithGenerics(Collection.class,
                        io.github.dbstarll.dubai.model.collection.test.o2.SimpleEntity.class)).length);
    }
}
