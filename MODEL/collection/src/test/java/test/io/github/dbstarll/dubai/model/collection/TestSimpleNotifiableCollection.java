package test.io.github.dbstarll.dubai.model.collection;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CountOptions;
import com.mongodb.client.model.DeleteOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndDeleteOptions;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.InsertManyOptions;
import com.mongodb.client.model.InsertOneOptions;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.collection.CollectionFactory;
import io.github.dbstarll.dubai.model.collection.CollectionWrapper;
import io.github.dbstarll.dubai.model.collection.test.MockMongoCursor;
import io.github.dbstarll.dubai.model.collection.test.SimpleNotifiableEntity;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestSimpleNotifiableCollection {
    @Injectable
    MongoDatabase mongoDatabase;

    @Mocked
    MongoCollection<SimpleNotifiableEntity> mongoCollection;

    @Mocked
    FindIterable<SimpleNotifiableEntity> findIterable;

    @Mocked
    DistinctIterable<String> distinctIterable;

    @Mocked
    UpdateResult updateResult;

    @Mocked
    DeleteResult deleteResult;

    @Mocked
    AggregateIterable<SimpleNotifiableEntity> aggregateIterable;

    private final Class<SimpleNotifiableEntity> entityClass = SimpleNotifiableEntity.class;

    private Collection<SimpleNotifiableEntity> collection;

    private MongoClientFactory mongoClientFactory;

    /**
     * 初始化collection.
     */
    @Before
    public void initialize() {
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
                times = 2;
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
        final SimpleNotifiableEntity entity = EntityFactory.newInstance(entityClass);
        assertNull(entity.getId());
        assertNull(entity.getDateCreated());
        assertNull(entity.getLastModified());

        final ObjectId id = new ObjectId();
        final SimpleNotifiableEntity savedEntity = collection.save(entity, id);
        assertSame(entity, savedEntity);
        assertNotNull(savedEntity.getId());
        assertNotNull(savedEntity.getDateCreated());
        assertNotNull(savedEntity.getLastModified());
        assertSame(id, savedEntity.getId());
        assertEquals(savedEntity.getDateCreated(), id.getDate());

        new Verifications() {
            {
                mongoCollection.insertOne((SimpleNotifiableEntity) any, (InsertOneOptions) any);
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

        final SimpleNotifiableEntity entity = EntityFactory.newInstance(entityClass);
        assertNull(entity.getId());
        assertNull(entity.getDateCreated());
        assertNull(entity.getLastModified());

        final SimpleNotifiableEntity savedEntity = collection.save(entity);
        assertSame(entity, savedEntity);
        assertNotNull(savedEntity.getId());
        assertNotNull(savedEntity.getDateCreated());
        assertNotNull(savedEntity.getLastModified());

        final ObjectId id = entity.getId();
        final Date dateCreated = entity.getDateCreated();
        final Date lastModified = entity.getLastModified();
        Thread.sleep(10);
        final SimpleNotifiableEntity savedAgainEntity = collection.save(savedEntity);
        assertSame(entity, savedAgainEntity);
        assertSame(id, savedAgainEntity.getId());
        assertSame(dateCreated, savedAgainEntity.getDateCreated());
        assertEquals(1, savedAgainEntity.getLastModified().compareTo(lastModified));

        new Verifications() {
            {
                mongoCollection.insertOne((SimpleNotifiableEntity) any, (InsertOneOptions) any);
                times = 1;
                mongoCollection.replaceOne((Bson) any, (SimpleNotifiableEntity) any, (ReplaceOptions) any);
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
        final SimpleNotifiableEntity entity = (SimpleNotifiableEntity) Proxy.newProxyInstance(entityClass.getClassLoader(),
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
        final SimpleNotifiableEntity entity = EntityFactory.newInstance(entityClass);
        new Expectations() {
            {
                mongoCollection.getCodecRegistry();
                result = mongoClientFactory.getMongoClientSettingsbuilder().build().getCodecRegistry();
                mongoCollection.findOneAndDelete((Bson) any, (FindOneAndDeleteOptions) any);
                result = entity;
            }
        };

        assertNull(collection.deleteById(null));
        assertSame(entity, collection.deleteById(new ObjectId()));

        new Verifications() {
            {
                mongoCollection.getCodecRegistry();
                times = 2;
                mongoCollection.findOneAndDelete((Bson) any, (FindOneAndDeleteOptions) any);
                times = 1;
            }
        };
    }

    @Test
    public void testFindById() {
        final SimpleNotifiableEntity entity = EntityFactory.newInstance(entityClass);
        new Expectations() {
            {
                mongoCollection.getCodecRegistry();
                result = mongoClientFactory.getMongoClientSettingsbuilder().build().getCodecRegistry();
                findIterable.first();
                result = entity;
            }
        };

        assertNull(collection.findById(null));
        assertSame(entity, collection.findById(new ObjectId()));

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
        final SimpleNotifiableEntity entity = EntityFactory.newInstance(entityClass);
        new Expectations() {
            {
                mongoCollection.getCodecRegistry();
                result = mongoClientFactory.getMongoClientSettingsbuilder().build().getCodecRegistry();
                findIterable.first();
                result = entity;
            }
        };

        assertSame(entity, collection.findOne());

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

        assertSame(findIterable, collection.findByIds(Collections.singleton(new ObjectId())));

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

        assertSame(findIterable, collection.find());
        assertSame(findIterable, collection.find(entityClass));

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
                mongoCollection.insertMany((List<SimpleNotifiableEntity>) any, (InsertManyOptions) any);
                times = 1;
            }
        };
    }

    @Test
    public void testDeleteOne() {
        new Expectations() {
            {
                mongoCollection.getCodecRegistry();
                result = mongoClientFactory.getMongoClientSettingsbuilder().build().getCodecRegistry();
                deleteResult.getDeletedCount();
                result = 1;
            }
        };

        assertEquals(1, collection.deleteOne(Filters.eq(new ObjectId())).getDeletedCount());

        new Verifications() {
            {
                mongoCollection.getCodecRegistry();
                times = 1;
                mongoCollection.deleteOne((Bson) any, (DeleteOptions) any);
                times = 1;
                deleteResult.getDeletedCount();
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
                findIterable.iterator();
                result = new MockMongoCursor<>(Collections.singletonList(EntityFactory.newInstance(entityClass)).iterator());
                deleteResult.getDeletedCount();
                result = 10;
            }
        };

        assertEquals(10, collection.deleteMany(Filters.eq(new ObjectId())).getDeletedCount());

        new Verifications() {
            {
                mongoCollection.getCodecRegistry();
                times = 1;
                mongoCollection.deleteMany((Bson) any, (DeleteOptions) any);
                times = 1;
                deleteResult.getDeletedCount();
                times = 1;
            }
        };
    }

    @Test
    public void testUpdateById() {
        final SimpleNotifiableEntity entity = EntityFactory.newInstance(entityClass);
        new Expectations() {
            {
                mongoCollection.getCodecRegistry();
                result = mongoClientFactory.getMongoClientSettingsbuilder().build().getCodecRegistry();
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
                mongoCollection.findOneAndUpdate((Bson) any, (Bson) any, (FindOneAndUpdateOptions) any);
                times = 1;
            }
        };
    }

    @Test
    public void testUpdateOne() {
        new Expectations() {
            {
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
            }
        };
    }

    @Test
    public void testFindOneAndDelete() {
        final SimpleNotifiableEntity entity = EntityFactory.newInstance(entityClass);
        new Expectations() {
            {
                mongoCollection.getCodecRegistry();
                result = mongoClientFactory.getMongoClientSettingsbuilder().build().getCodecRegistry();
                mongoCollection.findOneAndDelete((Bson) any, (FindOneAndDeleteOptions) any);
                result = entity;
            }
        };

        assertSame(entity, collection.findOneAndDelete(Filters.eq(new ObjectId())));

        new Verifications() {
            {
                mongoCollection.getCodecRegistry();
                times = 2;
                mongoCollection.findOneAndDelete((Bson) any, (FindOneAndDeleteOptions) any);
                times = 1;
            }
        };
    }

    @Test
    public void testFindOneAndReplace() {
        final SimpleNotifiableEntity entity = EntityFactory.newInstance(entityClass);
        new Expectations() {
            {
                mongoCollection.getCodecRegistry();
                result = mongoClientFactory.getMongoClientSettingsbuilder().build().getCodecRegistry();
                mongoCollection.findOneAndReplace((Bson) any, (SimpleNotifiableEntity) any, (FindOneAndReplaceOptions) any);
                result = entity;
            }
        };

        assertSame(entity, collection.findOneAndReplace(Filters.eq(new ObjectId()), entity));

        new Verifications() {
            {
                mongoCollection.getCodecRegistry();
                times = 2;
                mongoCollection.findOneAndReplace((Bson) any, (SimpleNotifiableEntity) any, (FindOneAndReplaceOptions) any);
                times = 1;
            }
        };
    }

    @Test
    public void testFindOneAndUpdate() {
        final SimpleNotifiableEntity entity = EntityFactory.newInstance(entityClass);
        new Expectations() {
            {
                mongoCollection.getCodecRegistry();
                result = mongoClientFactory.getMongoClientSettingsbuilder().build().getCodecRegistry();
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
    public void testOriginal() {
        assertSame(((CollectionWrapper<SimpleNotifiableEntity>) collection).getCollection(), collection.original());
    }
}
