package io.github.dbstarll.dubai.model.service.attach;

import com.mongodb.client.model.Filters;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.service.ServiceFactory;
import io.github.dbstarll.dubai.model.service.ServiceTestCase;
import io.github.dbstarll.dubai.model.service.test.TestEntity;
import io.github.dbstarll.dubai.model.service.test.TestEntityService;
import io.github.dbstarll.dubai.model.service.test3.namable.TestNamableEntity;
import io.github.dbstarll.dubai.model.service.test3.namable.TestNamableService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map.Entry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestCompanyAttach extends ServiceTestCase {
    private final Class<TestEntity> entityClass = TestEntity.class;
    private final Class<TestEntityService> serviceClass = TestEntityService.class;

    @BeforeAll
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

    @Test
    public void testFindWithCompany() {
        useCollectionFactory(cf -> {
            final TestEntityService service = ServiceFactory.newInstance(serviceClass, cf.newInstance(entityClass));
            final TestNamableService companyService = ServiceFactory.newInstance(TestNamableService.class,
                    cf.newInstance(TestNamableEntity.class));

            assertNull(service.findWithCompany(companyService, null).first());

            final TestNamableEntity company = EntityFactory.newInstance(TestNamableEntity.class);
            company.setName("测试公司");
            assertNotNull(companyService.save(company, null));

            final TestEntity entity = service.save(EntityFactory.newInstance(entityClass), null);
            assertNotNull(entity);

            final Entry<TestEntity, TestNamableEntity> match = service.findWithCompany(companyService, Filters.eq(entity.getId())).first();
            assertNotNull(match);
            assertEquals(entity, match.getKey());
            assertNull(match.getValue());

            entity.setCompanyId(company.getId());
            assertNotNull(service.save(entity, null));

            final Entry<TestEntity, TestNamableEntity> match2 = service.findWithCompany(companyService, Filters.eq(entity.getId())).first();
            assertNotNull(match2);
            assertEquals(entity, match2.getKey());
            assertEquals(company, match2.getValue());
        });
    }
}
