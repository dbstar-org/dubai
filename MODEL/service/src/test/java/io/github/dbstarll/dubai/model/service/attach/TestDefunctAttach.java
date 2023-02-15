package io.github.dbstarll.dubai.model.service.attach;

import com.mongodb.client.model.Filters;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.service.ServiceTestCase;
import io.github.dbstarll.dubai.model.service.test.TestEntity;
import io.github.dbstarll.dubai.model.service.test.TestEntityService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestDefunctAttach extends ServiceTestCase {
    private final Class<TestEntity> entityClass = TestEntity.class;
    private final Class<TestEntityService> serviceClass = TestEntityService.class;

    @BeforeAll
    static void setup() {
        globalCollectionFactory();
    }

    @Test
    void testContainsWithDefunct() {
        useService(serviceClass, s -> {
            assertFalse(s.contains(new ObjectId(), null));
            assertFalse(s.contains(new ObjectId(), true));
            assertFalse(s.contains(new ObjectId(), false));

            final TestEntity entity = s.save(EntityFactory.newInstance(entityClass), null);
            assertTrue(s.contains(entity.getId(), null));
            assertFalse(s.contains(entity.getId(), true));
            assertTrue(s.contains(entity.getId(), false));

            entity.setDefunct(true);
            assertEquals(entity, s.deleteById(entity.getId()));
            assertTrue(s.contains(entity.getId(), null));
            assertTrue(s.contains(entity.getId(), true));
            assertFalse(s.contains(entity.getId(), false));
        });
    }

    @Test
    void testFindWithDefunct() {
        useService(serviceClass, s -> {
            assertNull(s.find(Filters.eq(new ObjectId()), null).first());
            assertNull(s.find(Filters.eq(new ObjectId()), true).first());
            assertNull(s.find(Filters.eq(new ObjectId()), false).first());

            final TestEntity entity = s.save(EntityFactory.newInstance(entityClass), null);
            assertEquals(entity, s.find(Filters.eq(entity.getId()), null).first());
            assertNull(s.find(Filters.eq(entity.getId()), true).first());
            assertEquals(entity, s.find(Filters.eq(entity.getId()), false).first());

            entity.setDefunct(true);
            assertEquals(entity, s.deleteById(entity.getId()));
            assertEquals(entity, s.find(Filters.eq(entity.getId()), null).first());
            assertEquals(entity, s.find(Filters.eq(entity.getId()), true).first());
            assertNull(s.find(Filters.eq(entity.getId()), false).first());
        });
    }

    @Test
    void testFindByIdWithDefunct() {
        useService(serviceClass, s -> {
            assertNull(s.findById(new ObjectId(), null));
            assertNull(s.findById(new ObjectId(), true));
            assertNull(s.findById(new ObjectId(), false));

            final TestEntity entity = s.save(EntityFactory.newInstance(entityClass), null);
            assertEquals(entity, s.findById(entity.getId(), null));
            assertNull(s.findById(entity.getId(), true));
            assertEquals(entity, s.findById(entity.getId(), false));

            entity.setDefunct(true);
            assertEquals(entity, s.deleteById(entity.getId()));
            assertEquals(entity, s.findById(entity.getId(), null));
            assertEquals(entity, s.findById(entity.getId(), true));
            assertNull(s.findById(entity.getId(), false));
        });
    }

    @Test
    void testCountWithDefunct() {
        useService(serviceClass, s -> {
            assertEquals(0, s.count(Filters.eq(new ObjectId()), null));
            assertEquals(0, s.count(Filters.eq(new ObjectId()), true));
            assertEquals(0, s.count(Filters.eq(new ObjectId()), false));

            final TestEntity entity = s.save(EntityFactory.newInstance(entityClass), null);
            assertEquals(1, s.count(Filters.eq(entity.getId()), null));
            assertEquals(0, s.count(Filters.eq(entity.getId()), true));
            assertEquals(1, s.count(Filters.eq(entity.getId()), false));

            entity.setDefunct(true);
            assertEquals(entity, s.deleteById(entity.getId()));
            assertEquals(1, s.count(Filters.eq(entity.getId()), null));
            assertEquals(1, s.count(Filters.eq(entity.getId()), true));
            assertEquals(0, s.count(Filters.eq(entity.getId()), false));
        });
    }

    @Test
    void testCountNullWithDefunct() {
        useService(serviceClass, s -> {
            assertEquals(0, s.count(null, null));
            assertEquals(0, s.count(null, true));
            assertEquals(0, s.count(null, false));

            final TestEntity entity = s.save(EntityFactory.newInstance(entityClass), null);
            assertEquals(1, s.count(null, null));
            assertEquals(0, s.count(null, true));
            assertEquals(1, s.count(null, false));

            entity.setDefunct(true);
            assertEquals(entity, s.deleteById(entity.getId()));
            assertEquals(1, s.count(null, null));
            assertEquals(1, s.count(null, true));
            assertEquals(0, s.count(null, false));
        });
    }
}
