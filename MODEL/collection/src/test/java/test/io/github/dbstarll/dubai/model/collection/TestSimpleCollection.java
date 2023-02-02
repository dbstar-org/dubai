package test.io.github.dbstarll.dubai.model.collection;

import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import io.github.dbstarll.dubai.model.collection.test.Delay;
import io.github.dbstarll.dubai.model.collection.test.SimpleEntity;
import io.github.dbstarll.dubai.model.collection.test.SimpleEntity.Type;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.entity.EntityModifier;
import io.github.dbstarll.dubai.model.entity.info.Namable;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.junit.BeforeClass;
import org.junit.Test;
import test.io.github.dbstarll.dubai.model.MongodTestCase;

import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestSimpleCollection extends MongodTestCase {
    private final Class<SimpleEntity> entityClass = SimpleEntity.class;

    @BeforeClass
    public static void beforeClass() {
        globalCollectionFactory();
    }

    @Test
    public void testGetEntityClass() {
        useCollection(entityClass, c -> assertEquals(entityClass, c.getEntityClass()));
    }

    @Test
    public void testCount() {
        useCollection(entityClass, c -> {
            assertEquals(0, c.count());
            assertEquals(0, c.count(null));

            c.save(EntityFactory.newInstance(entityClass));
            assertEquals(1, c.count());
            assertEquals(1, c.count(null));

            c.save(EntityFactory.newInstance(entityClass));
            assertEquals(2, c.count());
            assertEquals(2, c.count(null));
        });
    }

    @Test
    public void testContains() {
        useCollection(entityClass, c -> {
            assertFalse(c.contains(new ObjectId()));
            assertTrue(c.contains(c.save(EntityFactory.newInstance(entityClass)).getId()));
        });
    }

    @Test
    public void testSaveWithId() {
        useCollection(entityClass, c -> {
            final SimpleEntity entity = EntityFactory.newInstance(entityClass);
            assertNull(entity.getId());
            assertNull(entity.getDateCreated());
            assertNull(entity.getLastModified());

            final ObjectId id = new ObjectId();
            final SimpleEntity savedEntity = c.save(entity, id);
            assertSame(entity, savedEntity);
            assertNotNull(savedEntity.getId());
            assertNotNull(savedEntity.getDateCreated());
            assertNotNull(savedEntity.getLastModified());
            assertSame(id, savedEntity.getId());
            assertEquals(savedEntity.getDateCreated(), id.getDate());
        });
    }

    @Test
    public void testSave() {
        useCollection(entityClass, c -> {
            final SimpleEntity entity = EntityFactory.newInstance(entityClass);
            assertNull(entity.getId());
            assertNull(entity.getDateCreated());
            assertNull(entity.getLastModified());

            final SimpleEntity savedEntity = c.save(entity);
            assertSame(entity, savedEntity);
            assertNotNull(savedEntity.getId());
            assertNotNull(savedEntity.getDateCreated());
            assertNotNull(savedEntity.getLastModified());

            final ObjectId id = entity.getId();
            final Date dateCreated = entity.getDateCreated();
            final Date lastModified = entity.getLastModified();

            try {
                Delay.delay();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            final SimpleEntity savedAgainEntity = c.save(savedEntity);
            assertSame(entity, savedAgainEntity);
            assertSame(id, savedAgainEntity.getId());
            assertSame(dateCreated, savedAgainEntity.getDateCreated());
            assertEquals(1, savedAgainEntity.getLastModified().compareTo(lastModified));
        });
    }

    @Test
    public void testSaveNull() {
        useCollection(entityClass, c -> assertNull(c.save(null)));
    }

    @Test
    public void testSaveNoEntityModifier() {
        useCollection(entityClass, c -> {
            final SimpleEntity entity = (SimpleEntity) Proxy.newProxyInstance(entityClass.getClassLoader(),
                    new Class[]{entityClass}, (proxy, method, args) -> null);
            final ObjectId id = new ObjectId();
            try {
                c.save(entity, id);
                fail("throw IllegalArgumentException");
            } catch (IllegalArgumentException ex) {
                assertTrue(ex.getMessage().startsWith("UnModify entity: "));
            }
        });
    }

    @Test
    public void testDeleteById() {
        useCollection(entityClass, c -> {
            final SimpleEntity entity = EntityFactory.newInstance(entityClass);

            assertNull(c.deleteById(null));

            assertNotNull(c.save(entity));

            assertEquals(entity, c.deleteById(entity.getId()));
        });
    }

    @Test
    public void testFindById() {
        useCollection(entityClass, c -> {
            final SimpleEntity entity = EntityFactory.newInstance(entityClass);

            assertNull(c.findById(null));

            assertNotNull(c.save(entity));

            assertEquals(entity, c.findById(entity.getId()));
        });
    }

    @Test
    public void testFindOne() {
        useCollection(entityClass, c -> {
            final SimpleEntity entity = EntityFactory.newInstance(entityClass);
            assertNotNull(c.save(entity));
            assertEquals(entity, c.findOne());
        });
    }

    @Test
    public void testFindByIds() {
        useCollection(entityClass, c -> {
            assertNull(null, c.findByIds(Collections.singleton(new ObjectId())).first());

            final SimpleEntity entity1 = c.save(EntityFactory.newInstance(entityClass));
            final SimpleEntity entity2 = c.save(EntityFactory.newInstance(entityClass));
            final SimpleEntity entity3 = c.save(EntityFactory.newInstance(entityClass));

            final Set<SimpleEntity> found = c.findByIds(Arrays.asList(entity1.getId(), entity3.getId()))
                    .into(new HashSet<>());
            assertEquals(2, found.size());
            assertTrue(found.contains(entity1));
            assertFalse(found.contains(entity2));
            assertTrue(found.contains(entity3));
        });
    }

    @Test
    public void testFind() {
        useCollection(entityClass, c -> {
            assertNull(null, c.find().first());
            assertNull(null, c.find(entityClass).first());

            final SimpleEntity entity = c.save(EntityFactory.newInstance(entityClass));

            assertEquals(entity, c.find().first());
            assertEquals(entity, c.find(entityClass).first());
        });
    }

    @Test
    public void testFindQuery() {
        useCollection(entityClass, c -> {
            assertNull(c.find(Filters.eq("type", SimpleEntity.Type.t1)).first());
            assertNull(c.find(Filters.eq("bytes", new byte[0])).first());

            final SimpleEntity entity1 = EntityFactory.newInstance(entityClass);
            entity1.setType(Type.t1);
            entity1.setBytes("entity1".getBytes(StandardCharsets.UTF_8));
            c.save(entity1);

            final SimpleEntity entity2 = EntityFactory.newInstance(entityClass);
            entity2.setType(Type.t2);
            entity2.setBytes(new byte[0]);
            c.save(entity2);

            final List<SimpleEntity> found1 = c.find(Filters.eq("type", SimpleEntity.Type.t1))
                    .into(new ArrayList<>());
            assertEquals(1, found1.size());
            assertEquals(entity1, found1.get(0));

            final List<SimpleEntity> found2 = c.find(Filters.eq("bytes", new byte[0]))
                    .into(new ArrayList<>());
            assertEquals(1, found2.size());
            assertEquals(entity2, found2.get(0));
        });
    }

    @Test
    public void testDistinct() {
        useCollection(entityClass, c -> {
            assertNull(c.distinct(Namable.FIELD_NAME_NAME, String.class).first());

            final SimpleEntity entity1 = EntityFactory.newInstance(entityClass);
            entity1.setType(Type.t1);
            c.save(entity1);

            final SimpleEntity entity2 = EntityFactory.newInstance(entityClass);
            entity2.setType(Type.t2);
            c.save(entity2);

            final SimpleEntity entity3 = EntityFactory.newInstance(entityClass);
            c.save(entity3);

            final Set<Type> found = c.distinct("type", Type.class).into(new HashSet<>());
            assertEquals(2, found.size());
            assertTrue(found.contains(Type.t1));
            assertTrue(found.contains(Type.t1));
        });
    }

    @Test
    public void testInsertMany() {
        useCollection(entityClass, c -> {
            assertEquals(0, c.count());
            c.insertMany(Arrays.asList(EntityFactory.newInstance(entityClass), EntityFactory.newInstance(entityClass)));
            assertEquals(2, c.count());
        });
    }

    @Test
    public void testDeleteOne() {
        useCollection(entityClass, c -> {
            assertEquals(0, c.deleteOne(Filters.eq(new ObjectId())).getDeletedCount());

            final SimpleEntity entity = c.save(EntityFactory.newInstance(entityClass));
            assertEquals(entity, c.findById(entity.getId()));

            assertEquals(1, c.deleteOne(Filters.eq(entity.getId())).getDeletedCount());
            assertNull(c.findById(entity.getId()));
        });
    }

    @Test
    public void testDeleteMany() {
        useCollection(entityClass, c -> {
            assertEquals(0, c.deleteMany(Filters.eq(new ObjectId())).getDeletedCount());

            final SimpleEntity entity1 = c.save(EntityFactory.newInstance(entityClass));
            final SimpleEntity entity2 = c.save(EntityFactory.newInstance(entityClass));
            assertEquals(entity1, c.findById(entity1.getId()));
            assertEquals(entity2, c.findById(entity2.getId()));

            assertEquals(2, c.deleteMany(Filters.in(Entity.FIELD_NAME_ID, entity1.getId(), entity2.getId()))
                    .getDeletedCount());
            assertNull(c.findById(entity1.getId()));
            assertNull(c.findById(entity2.getId()));
        });
    }

    @Test
    public void testUpdateById() {
        useCollection(entityClass, c -> {
            final SimpleEntity entity = EntityFactory.newInstance(entityClass);
            entity.setType(Type.t1);
            c.save(entity);

            assertNull(c.updateById(null, Updates.set("type", Type.t2)));

            final SimpleEntity updated = c.updateById(entity.getId(), Updates.set("type", Type.t2),
                    new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER));
            assertNotNull(updated);
            assertSame(Type.t2, updated.getType());
        });
    }

    @Test
    public void testUpdateOne() {
        useCollection(entityClass, c -> {

            final SimpleEntity entity = EntityFactory.newInstance(entityClass);
            entity.setType(Type.t1);
            c.save(entity);

            assertEquals(0, c.updateOne(Filters.eq(new ObjectId()), Updates.set("type", Type.t2)).getMatchedCount());

            final UpdateResult updated = c.updateOne(Filters.eq(entity.getId()), Updates.set("type", Type.t2));
            assertNotNull(updated);
            assertEquals(1, updated.getMatchedCount());
            assertEquals(1, updated.getModifiedCount());

            assertSame(Type.t2, c.findById(entity.getId()).getType());
        });
    }

    @Test
    public void testUpdateMany() {
        useCollection(entityClass, c -> {
            final SimpleEntity entity1 = EntityFactory.newInstance(entityClass);
            entity1.setType(Type.t1);
            entity1.setBytes("t1".getBytes(StandardCharsets.UTF_8));
            c.save(entity1);

            final SimpleEntity entity2 = EntityFactory.newInstance(entityClass);
            entity2.setType(Type.t2);
            entity2.setBytes("t2".getBytes(StandardCharsets.UTF_8));
            c.save(entity2);

            final SimpleEntity entity3 = EntityFactory.newInstance(entityClass);
            entity3.setType(Type.t1);
            c.save(entity3);

            final UpdateResult updated = c.updateMany(Filters.eq("type", Type.t1),
                    Updates.set("bytes", "new".getBytes(StandardCharsets.UTF_8)));

            assertEquals("new", new String(c.findById(entity1.getId()).getBytes(), StandardCharsets.UTF_8));
            assertEquals("t2", new String(c.findById(entity2.getId()).getBytes(), StandardCharsets.UTF_8));
            assertEquals("new", new String(c.findById(entity3.getId()).getBytes(), StandardCharsets.UTF_8));
        });
    }

    @Test
    public void testFindOneAndDelete() {
        useCollection(entityClass, c -> {
            final SimpleEntity entity1 = EntityFactory.newInstance(entityClass);
            entity1.setType(Type.t1);
            c.save(entity1);

            final SimpleEntity entity2 = EntityFactory.newInstance(entityClass);
            entity2.setType(Type.t2);
            c.save(entity2);

            assertNull(c.findOneAndDelete(Filters.eq(new ObjectId())));

            assertEquals(entity1, c.findOneAndDelete(Filters.eq("type", Type.t1)));
            assertNull(c.findById(entity1.getId()));
            assertEquals(entity2, c.findById(entity2.getId()));
        });
    }

    @Test
    public void testFindOneAndReplace() {
        useCollection(entityClass, c -> {
            final SimpleEntity entity1 = EntityFactory.newInstance(entityClass);
            entity1.setType(Type.t1);
            c.save(entity1);

            assertNull(c.findOneAndReplace(Filters.eq(new ObjectId()), entity1));

            final SimpleEntity entity2 = EntityFactory.newInstance(entityClass);
            entity2.setType(Type.t2);

            final SimpleEntity replaced = c.findOneAndReplace(Filters.eq(entity1.getId()), entity2,
                    new FindOneAndReplaceOptions().returnDocument(ReturnDocument.AFTER));
            assertNotNull(replaced);
            ((EntityModifier) entity2).setId(entity1.getId());
            assertEquals(entity2, replaced);
        });
    }

    @Test
    public void testFindOneAndUpdate() {
        useCollection(entityClass, c -> {
            final SimpleEntity entity = EntityFactory.newInstance(entityClass);
            entity.setType(Type.t1);
            c.save(entity);

            assertNull(c.findOneAndUpdate(Filters.eq(new ObjectId()), Updates.set("type", Type.t2)));

            final SimpleEntity updated = c.findOneAndUpdate(Filters.eq(entity.getId()), Updates.set("type", Type.t2),
                    new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER));
            assertNotNull(updated);
            assertSame(Type.t2, updated.getType());
        });
    }

    @Test
    public void testAggregate() {
        useCollection(entityClass, c -> {
            final List<Bson> pipelines = new ArrayList<>();
            pipelines.add(Aggregates.group("$type", Accumulators.sum("num", 1)));
            pipelines.add(Aggregates.sort(Sorts.ascending("_id")));
            assertNull(c.aggregate(pipelines, Document.class).first());

            final SimpleEntity entity1 = EntityFactory.newInstance(entityClass);
            entity1.setType(Type.t1);
            c.save(entity1);

            final SimpleEntity entity2 = EntityFactory.newInstance(entityClass);
            entity2.setType(Type.t2);
            c.save(entity2);

            assertEquals("[Document{{_id=t1, num=1}}, Document{{_id=t2, num=1}}]",
                    c.aggregate(pipelines, Document.class).into(new ArrayList<>()).toString());
        });
    }

    @Test
    public void testOriginal() {
        useCollection(entityClass, c -> assertSame(c, c.original()));
    }
}
