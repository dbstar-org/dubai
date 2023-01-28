package test.io.github.dbstarll.dubai.model.collection;

import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.mongodb.client.result.UpdateResult;
import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.collection.CollectionFactory;
import io.github.dbstarll.dubai.model.collection.test.CacheableEntity;
import io.github.dbstarll.dubai.model.collection.test.MockMongoCursor;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.entity.info.Namable;
import io.github.dbstarll.dubai.model.mongodb.MongoClientFactory;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Verifications;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class TestComplexCollection {
    @Injectable
    MongoDatabase mongoDatabase;

    @Mocked
    MongoCollection<CacheableEntity> mongoCollection;

    @Mocked
    FindIterable<CacheableEntity> findIterable;

    @Mocked
    DistinctIterable<String> distinctIterable;

    @Mocked
    UpdateResult updateResult;

    @Mocked
    AggregateIterable<CacheableEntity> aggregateIterable;

    @Mocked
    MapReduceIterable<CacheableEntity> mapReduceIterable;

    private final Class<CacheableEntity> entityClass = CacheableEntity.class;

    private Collection<CacheableEntity> collection;

    private MongoClientFactory mongoClientFactory;

    /**
     * 初始化collection.
     */
    @Before
    public void initialize() throws NoSuchAlgorithmException {
        final CollectionFactory collectionFactory = new CollectionFactory(mongoDatabase);
        this.collection = collectionFactory.newInstance(entityClass);
        this.mongoClientFactory = new MongoClientFactory();

        new Verifications() {
            {
                mongoDatabase.getCollection(anyString, entityClass);
                times = 1;
            }
        };
    }

    @Test
    public void testGetEntityClass() {
        new Expectations() {
            {
                mongoCollection.getDocumentClass();
                result = entityClass;
            }
        };

        assertEquals(entityClass, collection.getEntityClass());

        new Verifications() {
            {
                mongoCollection.getDocumentClass();
                times = 1;
            }
        };
    }

    @Test
    public void testCount() {
        new Expectations() {
            {
                mongoCollection.getCodecRegistry();
                result = mongoClientFactory.getMongoClientSettingsbuilder().build().getCodecRegistry();
                mongoCollection.countDocuments((Bson) any, (CountOptions) any);
                result = 10;
            }
        };

        assertEquals(10, collection.count());
        assertEquals(10, collection.count(null));
        assertTrue(collection.contains(new ObjectId()));

        new Verifications() {
            {
                mongoCollection.getCodecRegistry();
                times = 3;
                mongoCollection.countDocuments((Bson) any, (CountOptions) any);
                times = 3;
            }
        };
    }

    @Test
    public void testContains() {
        new Expectations() {
            {
                mongoCollection.getCodecRegistry();
                result = mongoClientFactory.getMongoClientSettingsbuilder().build().getCodecRegistry();
                mongoCollection.countDocuments((Bson) any, (CountOptions) any);
                result = 0;
            }
        };

        assertFalse(collection.contains(new ObjectId()));

        new Verifications() {
            {
                mongoCollection.getCodecRegistry();
                times = 1;
                mongoCollection.countDocuments((Bson) any, (CountOptions) any);
                times = 1;
            }
        };
    }

    @Test
    public void testSaveWithId() {
        final CacheableEntity entity = EntityFactory.newInstance(entityClass);
        assertNull(entity.getId());
        assertNull(entity.getDateCreated());
        assertNull(entity.getLastModified());

        final ObjectId id = new ObjectId();
        final CacheableEntity savedEntity = collection.save(entity, id);
        assertTrue(entity == savedEntity);
        assertNotNull(savedEntity.getId());
        assertNotNull(savedEntity.getDateCreated());
        assertNotNull(savedEntity.getLastModified());
        assertTrue(id == savedEntity.getId());
        assertEquals(savedEntity.getDateCreated(), id.getDate());

        new Verifications() {
            {
                mongoCollection.insertOne((CacheableEntity) any, (InsertOneOptions) any);
                times = 1;
            }
        };
    }

    @Test
    public void testSave() throws InterruptedException {
        new Expectations() {
            {
                mongoCollection.getCodecRegistry();
                result = mongoClientFactory.getMongoClientSettingsbuilder().build().getCodecRegistry();
            }
        };

        final CacheableEntity entity = EntityFactory.newInstance(entityClass);
        assertNull(entity.getId());
        assertNull(entity.getDateCreated());
        assertNull(entity.getLastModified());

        final CacheableEntity savedEntity = collection.save(entity);
        assertTrue(entity == savedEntity);
        assertNotNull(savedEntity.getId());
        assertNotNull(savedEntity.getDateCreated());
        assertNotNull(savedEntity.getLastModified());

        final ObjectId id = entity.getId();
        final Date dateCreated = entity.getDateCreated();
        final Date lastModified = entity.getLastModified();
        Thread.sleep(10);
        final CacheableEntity savedAgainEntity = collection.save(savedEntity);
        assertTrue(entity == savedAgainEntity);
        assertTrue(id == savedAgainEntity.getId());
        assertTrue(dateCreated == savedAgainEntity.getDateCreated());
        assertTrue(savedAgainEntity.getLastModified().compareTo(lastModified) == 1);

        new Verifications() {
            {
                mongoCollection.insertOne((CacheableEntity) any, (InsertOneOptions) any);
                times = 1;
                mongoCollection.replaceOne((Bson) any, (CacheableEntity) any, (ReplaceOptions) any);
                times = 1;
                mongoCollection.getCodecRegistry();
                times = 1;
            }
        };
    }

    @Test
    public void testSaveNull() {
        assertNull(collection.save(null));
    }

    @Test
    public void testSaveNoEntityModifier() {
        final CacheableEntity entity = (CacheableEntity) Proxy.newProxyInstance(entityClass.getClassLoader(),
                new Class[]{entityClass}, (proxy, method, args) -> null);

        try {
            collection.save(entity, new ObjectId());
            fail("throw IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().startsWith("UnModify entity: "));
        }
    }

    @Test
    public void testDeleteById() {
        final CacheableEntity entity = EntityFactory.newInstance(entityClass);
        new Expectations() {
            {
                mongoCollection.getCodecRegistry();
                result = mongoClientFactory.getMongoClientSettingsbuilder().build().getCodecRegistry();
                findIterable.first();
                result = entity;
                mongoCollection.findOneAndUpdate((Bson) any, (Bson) any, (FindOneAndUpdateOptions) any);
                result = entity;
            }
        };

        assertNull(collection.deleteById(null));
        assertTrue(entity == collection.deleteById(new ObjectId()));

        new Verifications() {
            {
                mongoCollection.getCodecRegistry();
                times = 2;
                findIterable.first();
                times = 1;
                mongoCollection.findOneAndUpdate((Bson) any, (Bson) any, (FindOneAndUpdateOptions) any);
                times = 1;
            }
        };
    }

    @Test
    public void testFindById() {
        final CacheableEntity entity = EntityFactory.newInstance(entityClass);
        new Expectations() {
            {
                mongoCollection.getCodecRegistry();
                result = mongoClientFactory.getMongoClientSettingsbuilder().build().getCodecRegistry();
                findIterable.first();
                result = entity;
            }
        };

        assertNull(collection.findById(null));
        assertTrue(entity.equals(collection.findById(new ObjectId())));

        new Verifications() {
            {
                mongoCollection.getCodecRegistry();
                times = 1;
                mongoCollection.find((Bson) any, (Class<?>) any);
                times = 1;
                findIterable.first();
                times = 1;
                findIterable.limit(1);
                times = 1;
            }
        };
    }

    @Test
    public void testFindOne() {
        final CacheableEntity entity = EntityFactory.newInstance(entityClass);
        new Expectations() {
            {
                mongoCollection.getCodecRegistry();
                result = mongoClientFactory.getMongoClientSettingsbuilder().build().getCodecRegistry();
                findIterable.first();
                result = entity;
            }
        };

        assertTrue(entity.equals(collection.findOne()));

        new Verifications() {
            {
                mongoCollection.getCodecRegistry();
                times = 1;
                mongoCollection.find((Bson) any, (Class<?>) any);
                times = 1;
                findIterable.first();
                times = 1;
                findIterable.limit(1);
                times = 1;
            }
        };
    }

    @Test
    public void testFindByIds() {
        new Expectations() {
            {
                mongoCollection.getCodecRegistry();
                result = mongoClientFactory.getMongoClientSettingsbuilder().build().getCodecRegistry();
            }
        };

        assertTrue(findIterable == collection.findByIds(Collections.singleton(new ObjectId())));

        new Verifications() {
            {
                mongoCollection.getCodecRegistry();
                times = 1;
                mongoCollection.find((Bson) any, (Class<?>) any);
                times = 1;
            }
        };
    }

    @Test
    public void testFind() {
        new Expectations() {
            {
                mongoCollection.getCodecRegistry();
                result = mongoClientFactory.getMongoClientSettingsbuilder().build().getCodecRegistry();
            }
        };

        assertTrue(findIterable == collection.find());
        assertTrue(findIterable == collection.find(entityClass));

        new Verifications() {
            {
                mongoCollection.getCodecRegistry();
                times = 2;
                mongoCollection.find((Bson) any, (Class<?>) any);
                times = 2;
            }
        };
    }

    @Test
    public void testDistinct() {
        new Expectations() {
            {
                mongoCollection.getCodecRegistry();
                result = mongoClientFactory.getMongoClientSettingsbuilder().build().getCodecRegistry();
            }
        };

        assertSame(distinctIterable, collection.distinct(Namable.FIELD_NAME_NAME, String.class));

        new Verifications() {
            {
                mongoCollection.getCodecRegistry();
                times = 1;
                mongoCollection.distinct(anyString, (Bson) any, String.class);
                times = 1;
            }
        };
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testInsertMany() {
        collection
                .insertMany(Arrays.asList(EntityFactory.newInstance(entityClass), EntityFactory.newInstance(entityClass)));

        new Verifications() {
            {
                mongoCollection.insertMany((List<CacheableEntity>) any, (InsertManyOptions) any);
                times = 1;
            }
        };
    }

    @Test
    public void testDeleteOne() {
        final CacheableEntity entity = EntityFactory.newInstance(entityClass);
        new Expectations() {
            {
                mongoCollection.getCodecRegistry();
                result = mongoClientFactory.getMongoClientSettingsbuilder().build().getCodecRegistry();
                findIterable.first();
                result = entity;
                updateResult.getModifiedCount();
                result = 1;
            }
        };

        assertEquals(1, collection.deleteOne(Filters.eq(new ObjectId())).getDeletedCount());

        new Verifications() {
            {
                mongoCollection.getCodecRegistry();
                times = 2;
                findIterable.first();
                times = 1;
                mongoCollection.updateOne((Bson) any, (Bson) any, (UpdateOptions) any);
                times = 1;
                updateResult.getModifiedCount();
                times = 1;
            }
        };
    }

    @Test
    public void testDeleteMany() {
        new Expectations() {
            {
                mongoCollection.getCodecRegistry();
                result = mongoClientFactory.getMongoClientSettingsbuilder().build().getCodecRegistry();
                updateResult.getModifiedCount();
                result = 10;
            }
        };

        assertEquals(10, collection.deleteMany(Filters.eq(new ObjectId())).getDeletedCount());

        new Verifications() {
            {
                mongoCollection.getCodecRegistry();
                times = 2;
                mongoCollection.updateMany((Bson) any, (Bson) any, (UpdateOptions) any);
                times = 1;
                updateResult.getModifiedCount();
                times = 1;
            }
        };
    }

    @Test
    public void testUpdateById() {
        final CacheableEntity entity = EntityFactory.newInstance(entityClass);
        new Expectations() {
            {
                mongoCollection.getCodecRegistry();
                result = mongoClientFactory.getMongoClientSettingsbuilder().build().getCodecRegistry();
                findIterable.first();
                result = entity;
                mongoCollection.findOneAndUpdate((Bson) any, (Bson) any, (FindOneAndUpdateOptions) any);
                result = entity;
            }
        };

        assertNull(collection.updateById(null, new Document(Namable.FIELD_NAME_NAME, "abc")));
        assertSame(entity, collection.updateById(new ObjectId(), new Document(Namable.FIELD_NAME_NAME, "abc")));

        new Verifications() {
            {
                mongoCollection.getCodecRegistry();
                times = 2;
                findIterable.first();
                times = 1;
                mongoCollection.findOneAndUpdate((Bson) any, (Bson) any, (FindOneAndUpdateOptions) any);
                times = 1;
            }
        };
    }

    @Test
    public void testUpdateOne() {
        final CacheableEntity entity = EntityFactory.newInstance(entityClass);
        new Expectations() {
            {
                findIterable.first();
                result = entity;
                mongoCollection.getCodecRegistry();
                result = mongoClientFactory.getMongoClientSettingsbuilder().build().getCodecRegistry();
            }
        };

        assertSame(updateResult,
                collection.updateOne(Filters.eq(new ObjectId()), new Document(Namable.FIELD_NAME_NAME, "abc")));

        new Verifications() {
            {
                mongoCollection.getCodecRegistry();
                times = 2;
                findIterable.first();
                times = 1;
                mongoCollection.updateOne((Bson) any, (Bson) any, (UpdateOptions) any);
                times = 1;
            }
        };
    }

    @Test
    public void testUpdateMany() {
        new Expectations() {
            {
                mongoCollection.getCodecRegistry();
                result = mongoClientFactory.getMongoClientSettingsbuilder().build().getCodecRegistry();
                findIterable.iterator();
                result = new MockMongoCursor<>(Collections.singletonList(EntityFactory.newInstance(entityClass)).iterator());
            }
        };

        assertSame(updateResult,
                collection.updateMany(Filters.eq(new ObjectId()), new Document(Namable.FIELD_NAME_NAME, "abc")));

        new Verifications() {
            {
                mongoCollection.getCodecRegistry();
                times = 2;
                mongoCollection.updateMany((Bson) any, (Bson) any, (UpdateOptions) any);
                times = 1;
                findIterable.iterator();
                times = 1;
            }
        };
    }

    @Test
    public void testFindOneAndDelete() {
        final CacheableEntity entity = EntityFactory.newInstance(entityClass);
        new Expectations() {
            {
                mongoCollection.getCodecRegistry();
                result = mongoClientFactory.getMongoClientSettingsbuilder().build().getCodecRegistry();
                findIterable.first();
                result = entity;
                mongoCollection.findOneAndUpdate((Bson) any, (Bson) any, (FindOneAndUpdateOptions) any);
                result = entity;
            }
        };

        assertSame(entity, collection.findOneAndDelete(Filters.eq(new ObjectId())));

        new Verifications() {
            {
                mongoCollection.getCodecRegistry();
                times = 2;
                findIterable.first();
                times = 1;
                mongoCollection.findOneAndUpdate((Bson) any, (Bson) any, (FindOneAndUpdateOptions) any);
                times = 1;
            }
        };
    }

    @Test
    public void testFindOneAndReplace() {
        final CacheableEntity entity = EntityFactory.newInstance(entityClass);
        new Expectations() {
            {
                mongoCollection.getCodecRegistry();
                result = mongoClientFactory.getMongoClientSettingsbuilder().build().getCodecRegistry();
                findIterable.first();
                result = entity;
                mongoCollection.findOneAndReplace((Bson) any, (CacheableEntity) any, (FindOneAndReplaceOptions) any);
                result = entity;
            }
        };

        assertSame(entity, collection.findOneAndReplace(Filters.eq(new ObjectId()), entity));

        new Verifications() {
            {
                mongoCollection.getCodecRegistry();
                times = 2;
                findIterable.first();
                times = 1;
                mongoCollection.findOneAndReplace((Bson) any, (CacheableEntity) any, (FindOneAndReplaceOptions) any);
                times = 1;
            }
        };
    }

    @Test
    public void testFindOneAndUpdate() {
        final CacheableEntity entity = EntityFactory.newInstance(entityClass);
        new Expectations() {
            {
                mongoCollection.getCodecRegistry();
                result = mongoClientFactory.getMongoClientSettingsbuilder().build().getCodecRegistry();
                findIterable.first();
                result = entity;
                mongoCollection.findOneAndUpdate((Bson) any, (Bson) any, (FindOneAndUpdateOptions) any);
                result = entity;
            }
        };

        assertSame(entity,
                collection.findOneAndUpdate(Filters.eq(new ObjectId()), new Document(Namable.FIELD_NAME_NAME, "abc")));

        new Verifications() {
            {
                mongoCollection.getCodecRegistry();
                times = 2;
                findIterable.first();
                times = 1;
                mongoCollection.findOneAndUpdate((Bson) any, (Bson) any, (FindOneAndUpdateOptions) any);
                times = 1;
            }
        };
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAggregate() {
        assertSame(aggregateIterable, collection.aggregate(Collections.singletonList(Filters.eq(new ObjectId()))));

        new Verifications() {
            {
                mongoCollection.aggregate((List<Bson>) any, (Class<?>) any);
                times = 1;
            }
        };
    }

    @Test
    public void testMapReduce() {
        assertSame(mapReduceIterable, collection.mapReduce("mapFunction", "reduceFunction"));

        new Verifications() {
            {
                mongoCollection.mapReduce(anyString, anyString, (Class<?>) any);
                times = 1;
            }
        };
    }

    @Test
    public void testOriginal() {
        assertFalse(collection == collection.original());
    }
}
