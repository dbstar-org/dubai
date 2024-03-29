package io.github.dbstarll.dubai.model.collection;

import io.github.dbstarll.dubai.model.MongodTestCase;
import io.github.dbstarll.dubai.model.cache.EntityCacheManager;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

class TestCollectionFactory extends MongodTestCase {
    @BeforeAll
    static void beforeClass() {
        globalCollectionFactory();
    }

    private <E extends Entity> void testGetCollection(final CollectionFactory cf, final Class<E> entityClass) {
        final Collection<E> collection = cf.newInstance(entityClass);
        assertNotNull(collection);
        assertEquals(entityClass, collection.getEntityClass());
        assertNotNull(CollectionFactory.getBaseCollection(collection));
    }

    @Test
    void testGetCollection() {
        useCollectionFactory(cf -> {
            testGetCollection(cf, InterfaceEntity.class);
            testGetCollection(cf, DefunctableEntity.class);
            testGetCollection(cf, NotifiableEntity.class);
            testGetCollection(cf, CacheableEntity.class);
        });
    }

    @Test
    void testGetCollectionInject() {
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
    void testGetCollectionNotEntity() {
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
    void testGetCollectionClassEntity() {
        useCollectionFactory(cf -> {
            try {
                cf.newInstance(ClassNoTableEntity.class);
                fail("throw RuntimeException");
            } catch (RuntimeException ex) {
                assertEquals(ex.getMessage(), "Invalid EntityClass: " + ClassNoTableEntity.class);
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetBaseCollectionNull() {
        final Collection<InterfaceEntity> collection = (Collection<InterfaceEntity>) Proxy.newProxyInstance(
                this.getClass().getClassLoader(), new Class[]{Collection.class}, (proxy, method, args) -> null);
        assertNull(CollectionFactory.getBaseCollection(collection));
    }
}
