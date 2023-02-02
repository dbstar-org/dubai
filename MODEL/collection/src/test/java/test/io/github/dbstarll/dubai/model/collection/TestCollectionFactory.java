package test.io.github.dbstarll.dubai.model.collection;

import io.github.dbstarll.dubai.model.cache.EntityCacheManager;
import io.github.dbstarll.dubai.model.collection.AnnotationCollectionNameGenerator;
import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.collection.CollectionFactory;
import io.github.dbstarll.dubai.model.collection.CollectionInitializeException;
import io.github.dbstarll.dubai.model.collection.CollectionNameGenerator;
import io.github.dbstarll.dubai.model.collection.test.CacheableEntity;
import io.github.dbstarll.dubai.model.collection.test.DefunctableEntity;
import io.github.dbstarll.dubai.model.collection.test.NotifiableEntity;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.test.ClassNoTableEntity;
import io.github.dbstarll.dubai.model.entity.test.InterfaceEntity;
import io.github.dbstarll.dubai.model.entity.test.NoTableEntity;
import io.github.dbstarll.dubai.model.notify.NotifyProvider;
import io.github.dbstarll.dubai.model.notify.NotifyType;
import org.bson.types.ObjectId;
import org.junit.BeforeClass;
import org.junit.Test;
import test.io.github.dbstarll.dubai.model.MongodTestCase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class TestCollectionFactory extends MongodTestCase {
    @BeforeClass
    public static void beforeClass() {
        globalCollectionFactory();
    }

    private <E extends Entity> void testGetCollection(final CollectionFactory cf, final Class<E> entityClass) {
        final Collection<E> collection = cf.newInstance(entityClass);
        assertNotNull(collection);
        assertEquals(entityClass, collection.getEntityClass());
    }

    @Test
    public void testGetCollection() {
        useCollectionFactory(cf -> {
            testGetCollection(cf, InterfaceEntity.class);
            testGetCollection(cf, DefunctableEntity.class);
            testGetCollection(cf, NotifiableEntity.class);
            testGetCollection(cf, CacheableEntity.class);
        });
    }

    @Test
    public void testGetCollectionInject() {
        final NotifyProvider notifyProvider = new NotifyProvider() {
            @Override
            public <E extends Entity> void doNotify(E e, NotifyType notifyType) {
                //do nothing
            }
        };
        final EntityCacheManager entityCacheManager = new EntityCacheManager() {
            @Override
            public <E extends Entity> E find(Class<E> entityClass, String key, UpdateCacheHandler<E> updateCacheHandler) {
                return null;
            }

            @Override
            public <E extends Entity> void update(Class<E> entityClass, ObjectId entityId, NotifyType notifyType) {
                //do nothing
            }

            @Override
            public <E extends Entity> void set(String key, E entity) {
                //do nothing
            }
        };
        final CollectionNameGenerator nameGenerator = new AnnotationCollectionNameGenerator();
        useCollectionFactory(cf -> {
            cf.setEntityCacheManager(entityCacheManager);
            cf.setNotifyProvider(notifyProvider);
            cf.setCollectionNameGenerator(nameGenerator);
            testGetCollection(cf, InterfaceEntity.class);
            testGetCollection(cf, DefunctableEntity.class);
            testGetCollection(cf, NotifiableEntity.class);
            testGetCollection(cf, CacheableEntity.class);
        });
    }

    @Test
    public void testGetCollectionNotEntity() {
        useCollectionFactory(cf -> {
            try {
                cf.newInstance(NoTableEntity.class);
                fail("throw CollectionInitializeException");
            } catch (CollectionInitializeException ex) {
                assertEquals(ex.getMessage(), "Invalid EntityClass: " + NoTableEntity.class);
            }
        });
    }

    @Test
    public void testGetCollectionClassEntity() {
        useCollectionFactory(cf -> {
            try {
                cf.newInstance(ClassNoTableEntity.class);
                fail("throw RuntimeException");
            } catch (RuntimeException ex) {
                assertEquals(ex.getMessage(), "Invalid EntityClass: " + ClassNoTableEntity.class);
            }
        });
    }
}
