package test.io.github.dbstarll.dubai.model.service.attach;

import com.mongodb.client.result.UpdateResult;
import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.service.ServiceFactory;
import io.github.dbstarll.dubai.model.service.attach.SourceAttach;
import io.github.dbstarll.dubai.model.service.test3.TestEntity;
import io.github.dbstarll.dubai.model.service.test3.TestService;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class TestSourceAttach {
    @Mocked
    Collection<TestEntity> collection;

    SourceAttach<TestEntity> service;

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
        this.service = ServiceFactory.newInstance(TestService.class, collection);
    }

    @After
    public void destory() {
        this.service = null;
    }

    @Test
    public void testMergeSource() {
        final UpdateResult updateResult = UpdateResult.acknowledged(20, 10L, null);
        new Expectations() {
            {
                collection.updateMany((Bson) any, (Bson) any);
                result = updateResult;
            }
        };

        assertEquals(updateResult, service.mergeSource("source", new ObjectId(), new ObjectId()));

        new Verifications() {
            {
                collection.getEntityClass();
                times = 1;
                collection.updateMany((Bson) any, (Bson) any);
                times = 1;
            }
        };
    }

    @Test
    public void testUpdateSource() {
        final ObjectId faceImageId = new ObjectId();
        final UpdateResult updateResult = UpdateResult.acknowledged(20, 10L, null);
        new Expectations() {
            {
                collection.updateMany((Bson) any, (Bson) any);
                result = updateResult;
            }
        };

        assertEquals(updateResult, service.updateSource(faceImageId, Collections.singletonMap("source", new ObjectId())));

        new Verifications() {
            {
                collection.getEntityClass();
                times = 1;
                collection.updateMany((Bson) any, (Bson) any);
                times = 1;
            }
        };
    }

    @Test
    public void testUpdateSourceNull() {
        try {
            service.updateSource(new ObjectId(), Collections.singletonMap("source", null));
        } catch (Throwable ex) {
            assertEquals(IllegalArgumentException.class, ex.getClass());
            assertEquals("来源不能包含空的key或者value", ex.getMessage());
        }
    }

    @Test
    public void testRemoveSource() {
        final ObjectId faceImageId = new ObjectId();
        final UpdateResult updateResult = UpdateResult.acknowledged(20, 10L, null);
        new Expectations() {
            {
                collection.updateMany((Bson) any, (Bson) any);
                result = updateResult;
            }
        };

        assertEquals(updateResult, service.removeSource(faceImageId, Collections.singletonMap("source", new ObjectId())));

        new Verifications() {
            {
                collection.getEntityClass();
                times = 1;
                collection.updateMany((Bson) any, (Bson) any);
                times = 1;
            }
        };
    }

    @Test
    public void testRemoveSourceNull() {
        try {
            service.removeSource(new ObjectId(), Collections.singletonMap("source", null));
        } catch (Throwable ex) {
            assertEquals(IllegalArgumentException.class, ex.getClass());
            assertEquals("来源不能包含空的key或者value", ex.getMessage());
        }
    }
}
