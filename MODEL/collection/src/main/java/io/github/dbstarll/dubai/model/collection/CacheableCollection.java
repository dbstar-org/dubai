package io.github.dbstarll.dubai.model.collection;

import io.github.dbstarll.dubai.model.cache.EntityCacheManager;
import io.github.dbstarll.dubai.model.cache.EntityCacheManager.UpdateCacheHandler;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.notify.NotifyType;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

public class CacheableCollection<E extends Entity> extends NotifiableCollection<E> {
    private EntityCacheManager entityCacheManager = new DefaultCacheManager();

    public CacheableCollection(Collection<E> collection) {
        super(collection);
    }

    public final void setEntityCacheManager(EntityCacheManager entityCacheManager) {
        this.entityCacheManager = entityCacheManager;
    }

    protected final EntityCacheManager getEntityCacheManager() {
        return entityCacheManager;
    }

    @Override
    public E findOne(final Bson filter) {
        final E val = entityCacheManager.find(getEntityClass(), filter.toString(), new UpdateCacheHandler<E>() {
            @Override
            public E updateCache(String key) {
                return collection.findOne(filter);
            }
        });

        return EntityFactory.clone(val);
    }

    private static class DefaultCacheManager implements EntityCacheManager {
        @Override
        public <E extends Entity> E find(Class<E> entityClass, String key, UpdateCacheHandler<E> updateCacheHandler) {
            return updateCacheHandler.updateCache(key);
        }

        @Override
        public <E extends Entity> void update(Class<E> entityClass, ObjectId entityId, NotifyType notifyType) {
            // do nothing
        }

        @Override
        public <E extends Entity> void set(String key, E entity) {
            // do nothing
        }
    }
}
