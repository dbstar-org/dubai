package test.io.github.dbstarll.dubai.model.collection;

import io.github.dbstarll.dubai.model.cache.EntityCacheManager;
import io.github.dbstarll.dubai.model.collection.CacheableCollection;
import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.collection.test.SimpleEntity;
import io.github.dbstarll.dubai.model.notify.NotifyType;
import mockit.Injectable;
import org.bson.types.ObjectId;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertTrue;

public class TestDefaultCacheManager {
    @Injectable
    Collection<SimpleEntity> collection;

    @Test
    public void test() {
        final EntityCacheManager entityCacheManager = new CacheableCollection<SimpleEntity>(collection) {
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

        entityCacheManager.update(SimpleEntity.class, new ObjectId(), NotifyType.insert);
    }
}
