package test.io.github.dbstarll.dubai.model.service.impl;

import com.mongodb.client.model.Filters;
import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.service.ServiceFactory;
import io.github.dbstarll.dubai.model.service.test3.TestEntity;
import io.github.dbstarll.dubai.model.service.test3.TestService;
import io.github.dbstarll.dubai.model.service.validate.DefaultValidate;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TestServiceImplemental {
    @Mocked
    Collection<TestEntity> collection;

    TestService service;

    /**
     * 初始化.
     */
    @Before
    public void setUp() {
        new Expectations() {
            {
                collection.getEntityClass();
                result = TestEntity.class;
            }
        };
        this.service = ServiceFactory.newInstance(TestService.class, collection);
    }

    @After
    public void tearDown() {
        this.service = null;
    }

    @Test
    public void testGetEntityClass() {
        assertEquals(TestEntity.class, service.getEntityClass());
        new Verifications() {
            {
                collection.getEntityClass();
                times = 2;
            }
        };
    }

    @Test
    public void testCount() {
        final Bson filter = Filters.eq(new ObjectId());
        new Expectations() {
            {
                collection.count(filter);
                result = 10;
            }
        };
        assertEquals(10, service.count(filter));
        new Verifications() {
            {
                collection.getEntityClass();
                times = 1;
                collection.count(filter);
                times = 1;
            }
        };
    }

    @Test
    public void testContains() {
        final ObjectId id = new ObjectId();
        new Expectations() {
            {
                collection.contains(id);
                result = true;
            }
        };
        assertTrue(service.contains(id));
        new Verifications() {
            {
                collection.getEntityClass();
                times = 1;
                collection.contains(id);
                times = 1;
            }
        };
    }

    @Test
    public void testFind() {
        final Bson filter = Filters.eq(new ObjectId());
        new Expectations() {
            {
                collection.find(filter);
                result = null;
            }
        };
        assertNull(service.find(filter));
        new Verifications() {
            {
                collection.getEntityClass();
                times = 1;
                collection.find(filter);
                times = 1;
            }
        };
    }

    @Test
    public void testFindOne() {
        final Bson filter = Filters.eq(new ObjectId());
        final TestEntity entity = EntityFactory.newInstance(TestEntity.class);
        new Expectations() {
            {
                collection.findOne(filter);
                result = entity;
            }
        };
        assertEquals(entity, service.findOne(filter));
        new Verifications() {
            {
                collection.getEntityClass();
                times = 1;
                collection.findOne(filter);
                times = 1;
            }
        };
    }

    @Test
    public void testFindById() {
        final ObjectId id = new ObjectId();
        final TestEntity entity = EntityFactory.newInstance(TestEntity.class);
        new Expectations() {
            {
                collection.findById(id);
                result = entity;
            }
        };
        assertEquals(entity, service.findById(id));
        new Verifications() {
            {
                collection.getEntityClass();
                times = 1;
                collection.findById(id);
                times = 1;
            }
        };
    }

    @Test
    public void testDeleteById() {
        final ObjectId id = new ObjectId();
        final TestEntity entity = EntityFactory.newInstance(TestEntity.class);
        new Expectations() {
            {
                collection.deleteById(id);
                result = entity;
            }
        };
        assertEquals(entity, service.deleteById(id));
        new Verifications() {
            {
                collection.getEntityClass();
                times = 1;
                collection.deleteById(id);
                times = 1;
            }
        };
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testSave() {
        final TestEntity entity = EntityFactory.newInstance(TestEntity.class);
        assertNotNull(service.save(entity));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testSaveWithId() {
        final ObjectId newEntityId = new ObjectId();
        final TestEntity entity = EntityFactory.newInstance(TestEntity.class);
        assertNotNull(service.save(entity, newEntityId));
    }

    @Test
    public void testSaveWithValidate() {
        final TestEntity entity = EntityFactory.newInstance(TestEntity.class);
        final DefaultValidate validate = new DefaultValidate();
        assertNotNull(service.save(entity, validate));
        assertFalse(validate.hasErrors());
    }

    @Test
    public void testDelay() throws InterruptedException {
        final AtomicReference<Object> lock1 = new AtomicReference<>();
        final AtomicReference<Object> lock2 = new AtomicReference<>();
        final Thread t1 = new Thread(() -> lock1.set(service.delay()));
        final Thread t2 = new Thread(() -> lock2.set(service.delay()));
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        assertNotNull(lock1.get());
        assertNotNull(lock2.get());
        assertSame(lock1.get(), lock2.get());
    }
}
