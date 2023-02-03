package test.io.github.dbstarll.dubai.model.service.impl;

import com.mongodb.client.model.Filters;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.service.test3.TestEntity;
import io.github.dbstarll.dubai.model.service.test3.TestService;
import io.github.dbstarll.dubai.model.service.validate.DefaultValidate;
import org.bson.types.ObjectId;
import org.junit.BeforeClass;
import org.junit.Test;
import test.io.github.dbstarll.dubai.model.service.ServiceTestCase;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TestServiceImplemental extends ServiceTestCase {
    private final Class<TestEntity> entityClass = TestEntity.class;
    private final Class<TestService> serviceClass = TestService.class;

    @BeforeClass
    public static void setup() {
        globalCollectionFactory();
    }

    @Test
    public void testGetEntityClass() {
        useService(serviceClass, s -> assertEquals(entityClass, s.getEntityClass()));
    }

    @Test
    public void testCount() {
        useService(serviceClass, s -> {
            assertEquals(0, s.count(null));
            assertEquals(0, s.count(Filters.eq(new ObjectId())));

            final TestEntity saved = s.save(EntityFactory.newInstance(entityClass), null);
            assertNotNull(saved);
            assertEquals(1, s.count(null));
            assertEquals(0, s.count(Filters.eq(new ObjectId())));
            assertEquals(1, s.count(Filters.eq(saved.getId())));
        });
    }

    @Test
    public void testContains() {
        useService(serviceClass, s -> {
            final TestEntity saved = s.save(EntityFactory.newInstance(entityClass), null);
            assertNotNull(saved);

            assertFalse(s.contains(new ObjectId()));
            assertTrue(s.contains(saved.getId()));
        });
    }

    @Test
    public void testFind() {
        useService(serviceClass, s -> {
            final TestEntity saved = s.save(EntityFactory.newInstance(entityClass), null);
            assertNotNull(saved);

            assertNull(s.find(Filters.eq(new ObjectId())).first());
            assertEquals(saved, s.find(Filters.eq(saved.getId())).first());
        });
    }

    @Test
    public void testFindOne() {
        useService(serviceClass, s -> {
            final TestEntity saved = s.save(EntityFactory.newInstance(entityClass), null);
            assertNotNull(saved);

            assertNull(s.findOne(Filters.eq(new ObjectId())));
            assertEquals(saved, s.findOne(Filters.eq(saved.getId())));
        });
    }

    @Test
    public void testFindById() {
        useService(serviceClass, s -> {
            final TestEntity saved = s.save(EntityFactory.newInstance(entityClass), null);
            assertNotNull(saved);

            assertNull(s.findById(new ObjectId()));
            assertEquals(saved, s.findById(saved.getId()));
        });
    }

    @Test
    public void testDeleteById() {
        useService(serviceClass, s -> {
            final TestEntity saved = s.save(EntityFactory.newInstance(entityClass), null);
            assertNotNull(saved);
            assertNull(s.deleteById(new ObjectId()));
            saved.setDefunct(true);
            assertEquals(saved, s.deleteById(saved.getId()));
            assertNull(s.deleteById(saved.getId()));
        });
    }

    @Test
    public void testSaveWithValidate() {
        useService(serviceClass, s -> {
            final TestEntity entity = EntityFactory.newInstance(entityClass);
            final DefaultValidate validate = new DefaultValidate();
            assertNotNull(s.save(entity, validate));
            assertFalse(validate.hasErrors());
        });
    }

    @Test
    public void testDelay() {
        useService(serviceClass, s -> {
            final AtomicReference<Object> lock1 = new AtomicReference<>();
            final AtomicReference<Object> lock2 = new AtomicReference<>();
            final Thread t1 = new Thread(() -> lock1.set(s.delay()));
            final Thread t2 = new Thread(() -> lock2.set(s.delay()));
            t1.start();
            t2.start();
            try {
                t1.join();
                t2.join();
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
            assertNotNull(lock1.get());
            assertNotNull(lock2.get());
            assertSame(lock1.get(), lock2.get());
        });
    }
}
