package io.github.dbstarll.dubai.model.collection;

import io.github.dbstarll.dubai.model.MongodTestCase;
import io.github.dbstarll.dubai.model.cache.EntityCacheManager;
import io.github.dbstarll.dubai.model.collection.test.SimpleEntity;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.notify.NotifyType;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TestDefaultCacheManager extends MongodTestCase {
    private final Class<SimpleEntity> entityClass = SimpleEntity.class;

    @Test
    void test() {
        useCollection(entityClass, c -> {
            final EntityCacheManager entityCacheManager = new CacheableCollection<SimpleEntity>(c) {
                public EntityCacheManager cacheManager() {
                    return getEntityCacheManager();
                }
            }.cacheManager();

            final AtomicBoolean call = new AtomicBoolean(false);
            entityCacheManager.find(SimpleEntity.class, new ObjectId().toHexString(),
                    key -> {
                        call.set(true);
                        return null;
                    });
            assertTrue(call.get());

            entityCacheManager.update(SimpleEntity.class, new ObjectId(), NotifyType.INSERT);
            entityCacheManager.set("cacheKey", EntityFactory.newInstance(SimpleEntity.class));
        });
    }
}
