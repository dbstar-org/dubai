package test.io.github.dbstarll.dubai.model.collection;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.collection.CollectionFactory;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.entity.test.NoCloneEntity;
import io.github.dbstarll.dubai.model.mongodb.MongoClientFactory;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Verifications;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.*;

public class TestCacheableCollectionCloneNotSupported {
    @Injectable
    MongoDatabase mongoDatabase;

    @Mocked
    MongoCollection<NoCloneEntity> mongoCollection;

    @Mocked
    FindIterable<NoCloneEntity> findIterable;

    private final Class<NoCloneEntity> entityClass = NoCloneEntity.class;

    private Collection<NoCloneEntity> collection;

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
    public void testFindById() {
        final NoCloneEntity entity = EntityFactory.newInstance(entityClass);
        new Expectations() {
            {
                mongoCollection.getCodecRegistry();
                result = mongoClientFactory.getMongoClientSettingsbuilder().build().getCodecRegistry();
                findIterable.first();
                result = entity;
            }
        };

        assertNull(collection.findById(null));

        try {
            collection.findById(new ObjectId());
            fail("throw CloneNotSupportedException");
        } catch (Throwable ex) {
            assertEquals(UnsupportedOperationException.class, ex.getClass());
            assertEquals(CloneNotSupportedException.class, ex.getCause().getClass());
        }

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
    public void testFindByIdNull() {
        new Expectations() {
            {
                mongoCollection.getCodecRegistry();
                result = mongoClientFactory.getMongoClientSettingsbuilder().build().getCodecRegistry();
                findIterable.first();
                result = null;
            }
        };

        assertNull(collection.findById(new ObjectId()));

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
}
