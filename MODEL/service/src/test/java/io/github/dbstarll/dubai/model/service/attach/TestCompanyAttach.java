package io.github.dbstarll.dubai.model.service.attach;

import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.service.ServiceTestCase;
import io.github.dbstarll.dubai.model.service.test.TestEntity;
import io.github.dbstarll.dubai.model.service.test.TestEntityService;
import org.bson.types.ObjectId;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class TestCompanyAttach extends ServiceTestCase {
    private final Class<TestEntity> entityClass = TestEntity.class;
    private final Class<TestEntityService> serviceClass = TestEntityService.class;

    @BeforeClass
    public static void setup() {
        globalCollectionFactory();
    }

    @Test
    public void testCountByCompanyId() {
        useService(serviceClass, s -> {
            final ObjectId companyId = new ObjectId();
            final TestEntity entity = EntityFactory.newInstance(entityClass);
            entity.setCompanyId(companyId);

            assertNotNull(s.save(entity, null));

            assertEquals(0, s.countByCompanyId(new ObjectId()));
            assertEquals(1, s.countByCompanyId(companyId));
        });
    }

    @Test
    public void testFindByCompanyId() {
        useService(serviceClass, s -> {
            final ObjectId companyId = new ObjectId();
            final TestEntity entity = EntityFactory.newInstance(entityClass);
            entity.setCompanyId(companyId);

            assertNotNull(s.save(entity, null));

            assertNull(s.findByCompanyId(new ObjectId()).first());
            assertEquals(entity, s.findByCompanyId(companyId).first());
        });
    }

    @Test
    public void testDeleteByCompanyId() {
        useService(serviceClass, s -> {
            final ObjectId companyId = new ObjectId();
            final TestEntity entity = EntityFactory.newInstance(entityClass);
            entity.setCompanyId(companyId);

            assertNotNull(s.save(entity, null));

            assertEquals(0, s.deleteByCompanyId(new ObjectId()).getDeletedCount());
            assertEquals(1, s.deleteByCompanyId(companyId).getDeletedCount());
        });
    }
}
