package test.io.github.dbstarll.dubai.model.service.attach;

import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.service.test3.TestEntity;
import io.github.dbstarll.dubai.model.service.test3.TestService;
import org.bson.types.ObjectId;
import org.junit.BeforeClass;
import org.junit.Test;
import test.io.github.dbstarll.dubai.model.service.ServiceTestCase;

import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

public class TestSourceAttach extends ServiceTestCase {
    private final Class<TestEntity> entityClass = TestEntity.class;
    private final Class<TestService> serviceClass = TestService.class;

    @BeforeClass
    public static void setup() {
        globalCollectionFactory();
    }

    @Test
    public void testMergeSource() {
        useService(serviceClass, s -> {
            final ObjectId from = new ObjectId();
            final ObjectId to = new ObjectId();

            assertEquals(0, s.mergeSource("test", from, to).getModifiedCount());

            final TestEntity entity = EntityFactory.newInstance(entityClass);
            entity.setSources(Collections.singletonMap("test", from));
            assertSame(entity, s.save(entity, null));

            assertEquals(1, s.mergeSource("test", from, to).getModifiedCount());

            final TestEntity loaded = s.findById(entity.getId());
            assertNotNull(loaded);
            assertEquals(to, loaded.getSources().get("test"));
        });
    }

    @Test
    public void testUpdateSource() {
        useService(serviceClass, s -> {
            final ObjectId src = new ObjectId();
            final ObjectId from = new ObjectId();
            final ObjectId from2 = new ObjectId();
            final ObjectId to = new ObjectId();

            final TestEntity entity = EntityFactory.newInstance(entityClass);
            entity.setSources(new HashMap<String, ObjectId>() {{
                put("src", src);
                put("from", from);
            }});
            assertSame(entity, s.save(entity, null));

            assertEquals(1, s.updateSource(entity.getId(), new HashMap<String, ObjectId>() {{
                put("from", from2);
                put("to", to);
            }}).getModifiedCount());

            final TestEntity loaded = s.findById(entity.getId());
            assertNotNull(loaded);
            assertEquals(3, loaded.getSources().size());
            assertEquals(src, loaded.getSources().get("src"));
            assertEquals(from2, loaded.getSources().get("from"));
            assertEquals(to, loaded.getSources().get("to"));
        });
    }

    @Test
    public void testUpdateSourceNull() {
        useService(serviceClass, s -> {
            try {
                s.updateSource(new ObjectId(), Collections.singletonMap("source", null));
            } catch (Throwable ex) {
                assertEquals(IllegalArgumentException.class, ex.getClass());
                assertEquals("来源不能包含空的key或者value", ex.getMessage());
            }
        });
    }

    @Test
    public void testRemoveSource() {
        useService(serviceClass, s -> {
            final ObjectId src = new ObjectId();
            final ObjectId from = new ObjectId();
            final ObjectId from2 = new ObjectId();

            final TestEntity entity = EntityFactory.newInstance(entityClass);
            entity.setSources(new HashMap<String, ObjectId>() {{
                put("src", src);
                put("from", from);
            }});
            assertSame(entity, s.save(entity, null));

            assertEquals(0, s.removeSource(entity.getId(), Collections.singletonMap("from", from2)).getModifiedCount());

            assertEquals(1, s.removeSource(entity.getId(), Collections.singletonMap("from", from)).getModifiedCount());

            final TestEntity loaded = s.findById(entity.getId());
            assertNotNull(loaded);
            assertEquals(1, loaded.getSources().size());
            assertEquals(src, loaded.getSources().get("src"));
        });
    }

    @Test
    public void testRemoveSourceNull() {
        useService(serviceClass, s -> {
            try {
                s.removeSource(new ObjectId(), Collections.singletonMap("source", null));
            } catch (Throwable ex) {
                assertEquals(IllegalArgumentException.class, ex.getClass());
                assertEquals("来源不能包含空的key或者value", ex.getMessage());
            }
        });
    }
}
