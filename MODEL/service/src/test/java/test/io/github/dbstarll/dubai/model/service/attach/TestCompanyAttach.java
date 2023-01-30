package test.io.github.dbstarll.dubai.model.service.attach;

import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.service.ServiceFactory;
import io.github.dbstarll.dubai.model.service.attach.CompanyAttach;
import io.github.dbstarll.dubai.model.service.test.TestEntity;
import io.github.dbstarll.dubai.model.service.test.TestEntityService;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestCompanyAttach {
    @Mocked
    Collection<TestEntity> collection;

    CompanyAttach<TestEntity> service;

    /**
     * 测试初始化.
     */
    @Before
    public void init() {
        new Expectations() {
            {
                collection.getEntityClass();
                result = TestEntity.class;
            }
        };
        this.service = ServiceFactory.newInstance(TestEntityService.class, collection);
    }

    @After
    public void destory() {
        this.service = null;
    }

    @Test
    public void testCountByCompanyId() {
        new Expectations() {
            {
                collection.count((Bson) any);
                result = 10;
            }
        };

        assertEquals(10, service.countByCompanyId(new ObjectId()));

        new Verifications() {
            {
                collection.getEntityClass();
                times = 2;
                collection.count((Bson) any);
                times = 1;
            }
        };
    }

    @Test
    public void testFindByCompanyId() {
        service.findByCompanyId(new ObjectId());

        new Verifications() {
            {
                collection.getEntityClass();
                times = 3;
                collection.find((Bson) any);
                times = 1;
            }
        };
    }

    @Test
    public void testDeleteByCompanyId() {
        service.deleteByCompanyId(new ObjectId());

        new Verifications() {
            {
                collection.getEntityClass();
                times = 2;
                collection.deleteMany((Bson) any);
                times = 1;
            }
        };
    }
}
