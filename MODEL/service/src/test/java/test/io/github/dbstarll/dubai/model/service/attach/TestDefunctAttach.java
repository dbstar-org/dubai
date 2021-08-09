package test.io.github.dbstarll.dubai.model.service.attach;

import com.mongodb.client.model.Filters;
import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.service.ServiceFactory;
import io.github.dbstarll.dubai.model.service.attach.DefunctAttach;
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

public class TestDefunctAttach {
    @Mocked
    Collection<TestEntity> collection;

    DefunctAttach<TestEntity> service;

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
    public void testContainsWithDefunctNull() {
        new Expectations() {
            {
                collection.contains((ObjectId) any);
                result = true;
            }
        };

        assertEquals(true, service.contains(new ObjectId(), null));

        new Verifications() {
            {
                collection.getEntityClass();
                times = 1;
                collection.contains((ObjectId) any);
                times = 1;
                collection.original();
                times = 1;
            }
        };
    }

    @Test
    public void testContainsWithDefunctTrue() {
        new Expectations() {
            {
                collection.count((Bson) any);
                result = 10;
            }
        };

        assertEquals(true, service.contains(new ObjectId(), true));

        new Verifications() {
            {
                collection.getEntityClass();
                times = 1;
                collection.count((Bson) any);
                times = 1;
                collection.original();
                times = 1;
            }
        };
    }

    @Test
    public void testContainsWithDefunctTrue2() {
        new Expectations() {
            {
                collection.count((Bson) any);
                result = 0;
            }
        };

        assertEquals(false, service.contains(new ObjectId(), true));

        new Verifications() {
            {
                collection.getEntityClass();
                times = 1;
                collection.count((Bson) any);
                times = 1;
                collection.original();
                times = 1;
            }
        };
    }

    @Test
    public void testContainsWithDefunctFalse() {
        new Expectations() {
            {
                collection.contains((ObjectId) any);
                result = true;
            }
        };

        assertEquals(true, service.contains(new ObjectId(), false));

        new Verifications() {
            {
                collection.getEntityClass();
                times = 2;
                collection.contains((ObjectId) any);
                times = 1;
                collection.original();
                times = 0;
            }
        };
    }

    @Test
    public void testFindWithDefunctNull() {
        service.find(Filters.eq(new ObjectId()), null);

        new Verifications() {
            {
                collection.getEntityClass();
                times = 2;
                collection.find((Bson) any);
                times = 1;
                collection.original();
                times = 1;
            }
        };
    }

    @Test
    public void testFindWithDefunctTrue() {
        service.find(Filters.eq(new ObjectId()), true);

        new Verifications() {
            {
                collection.getEntityClass();
                times = 2;
                collection.find((Bson) any);
                times = 1;
                collection.original();
                times = 1;
            }
        };
    }

    @Test
    public void testFindWithDefunctFalse() {
        service.find(Filters.eq(new ObjectId()), false);

        new Verifications() {
            {
                collection.getEntityClass();
                times = 3;
                collection.find((Bson) any);
                times = 1;
                collection.original();
                times = 0;
            }
        };
    }

    @Test
    public void testFindByIdWithDefunctNull() {
        service.findById(new ObjectId(), null);

        new Verifications() {
            {
                collection.getEntityClass();
                times = 2;
                collection.findById((ObjectId) any);
                times = 1;
                collection.original();
                times = 1;
            }
        };
    }

    @Test
    public void testFindByIdWithDefunctTrue() {
        service.findById(new ObjectId(), true);

        new Verifications() {
            {
                collection.getEntityClass();
                times = 2;
                collection.findOne((Bson) any);
                times = 1;
                collection.original();
                times = 1;
            }
        };
    }

    @Test
    public void testFindByIdWithDefunctFalse() {
        service.findById(new ObjectId(), false);

        new Verifications() {
            {
                collection.getEntityClass();
                times = 3;
                collection.findById((ObjectId) any);
                times = 1;
                collection.original();
                times = 0;
            }
        };
    }

    @Test
    public void testCountWithDefunctNull() {
        final Bson filter = Filters.eq(new ObjectId());

        new Expectations() {
            {
                collection.count(filter);
                result = 10;
            }
        };

        assertEquals(10, service.count(filter, null));

        new Verifications() {
            {
                collection.getEntityClass();
                times = 1;
                collection.count(filter);
                times = 1;
                collection.original();
                times = 1;
            }
        };
    }

    @Test
    public void testCountWithDefunctTrue() {
        final Bson filter = Filters.eq(new ObjectId());
        final Bson filterByDefunct = Filters.and(filter, service.filterByDefunct(true));

        new Expectations() {
            {
                collection.count(withInstanceLike(filterByDefunct));
                result = 20;
            }
        };

        assertEquals(20, service.count(filter, true));

        new Verifications() {
            {
                collection.getEntityClass();
                times = 0;
                collection.count(filter);
                times = 0;
                collection.count(withInstanceLike(filterByDefunct));
                times = 1;
                collection.original();
                times = 1;
            }
        };
    }

    @Test
    public void testCountWithDefunctFalse() {
        final Bson filter = Filters.eq(new ObjectId());

        new Expectations() {
            {
                collection.count(filter);
                result = 10;
            }
        };

        assertEquals(10, service.count(filter, false));

        new Verifications() {
            {
                collection.getEntityClass();
                times = 2;
                collection.count(filter);
                times = 1;
            }
        };
    }

    @Test
    public void testCountWithDefunctTrueFilterNull() {
        final Bson filterByDefunct = service.filterByDefunct(true);

        new Expectations() {
            {
                collection.count(withInstanceLike(filterByDefunct));
                result = 20;
            }
        };

        assertEquals(20, service.count(null, true));

        new Verifications() {
            {
                collection.getEntityClass();
                times = 0;
                collection.count(withInstanceLike(filterByDefunct));
                times = 1;
                collection.original();
                times = 1;
            }
        };
    }
}
