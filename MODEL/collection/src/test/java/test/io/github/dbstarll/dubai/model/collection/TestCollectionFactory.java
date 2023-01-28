package test.io.github.dbstarll.dubai.model.collection;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.github.dbstarll.dubai.model.cache.EntityCacheManager;
import io.github.dbstarll.dubai.model.collection.AnnotationCollectionNameGenerator;
import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.collection.CollectionFactory;
import io.github.dbstarll.dubai.model.collection.CollectionInitializeException;
import io.github.dbstarll.dubai.model.collection.test.CacheableEntity;
import io.github.dbstarll.dubai.model.collection.test.DefunctableEntity;
import io.github.dbstarll.dubai.model.collection.test.NotifiableEntity;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.test.ClassNoTableEntity;
import io.github.dbstarll.dubai.model.entity.test.InterfaceEntity;
import io.github.dbstarll.dubai.model.entity.test.NoTableEntity;
import io.github.dbstarll.dubai.model.notify.NotifyProvider;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestCollectionFactory {
    @Tested
    CollectionFactory collectionFactory;

    @Injectable
    MongoDatabase mongoDatabase;

    @Mocked
    MongoCollection<?> mongoCollection;

    private <E extends Entity> void testGetCollection(final Class<E> entityClass) {
        new Expectations() {
            {
                mongoDatabase.getCollection(anyString, entityClass);
                result = mongoCollection;
                mongoCollection.getDocumentClass();
                result = entityClass;
            }
        };

        final Collection<E> collection = collectionFactory.newInstance(entityClass);
        assertEquals(entityClass, collection.getEntityClass());

        new Verifications() {
            {
                mongoDatabase.getCollection(anyString, entityClass);
                times = 1;
                mongoCollection.getDocumentClass();
                times = 1;
            }
        };
    }

    @Test
    public void testGetCollection() {
        testGetCollection(InterfaceEntity.class);
        testGetCollection(DefunctableEntity.class);
        testGetCollection(NotifiableEntity.class);
        testGetCollection(CacheableEntity.class);
    }

    @Test
    public void testGetCollectionInject(@Mocked NotifyProvider notifyProvider,
                                        @Mocked EntityCacheManager entityCacheManager) {
        collectionFactory.setEntityCacheManager(entityCacheManager);
        collectionFactory.setNotifyProvider(notifyProvider);
        collectionFactory.setCollectionNameGenerator(new AnnotationCollectionNameGenerator());

        testGetCollection(InterfaceEntity.class);
        testGetCollection(DefunctableEntity.class);
        testGetCollection(NotifiableEntity.class);
        testGetCollection(CacheableEntity.class);
    }

    @Test
    public void testGetCollectionNotEntity() {
        try {
            collectionFactory.newInstance(NoTableEntity.class);
            fail("throw CollectionInitializeException");
        } catch (CollectionInitializeException ex) {
            assertEquals(ex.getMessage(), "Invalid EntityClass: " + NoTableEntity.class);
        }
    }

    @Test
    public void testGetCollectionClassEntity() {
        try {
            collectionFactory.newInstance(ClassNoTableEntity.class);
            fail("throw RuntimeException");
        } catch (RuntimeException ex) {
            assertEquals(ex.getMessage(), "Invalid EntityClass: " + ClassNoTableEntity.class);
        }
    }
}
