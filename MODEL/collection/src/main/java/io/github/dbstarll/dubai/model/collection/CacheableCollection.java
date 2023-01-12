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

    /**
     * 为collection叠加缓存功能.
     *
     * @param collection collection
     */
    public CacheableCollection(final Collection<E> collection) {
        super(collection);
    }

    /**
     * 设置外部的缓存管理器，替代默认实现.
     *
     * @param entityCacheManager 缓存管理器
     */
    public final void setEntityCacheManager(final EntityCacheManager entityCacheManager) {
        this.entityCacheManager = entityCacheManager;
    }

    protected final EntityCacheManager getEntityCacheManager() {
        return entityCacheManager;
    }

    @Override
    public E findOne(final Bson filter) {
        final E val = entityCacheManager.find(getEntityClass(), filter.toString(), new UpdateCacheHandler<E>() {
            @Override
            public E updateCache(final String key) {
                return collection.findOne(filter);
            }
        });

        return EntityFactory.clone(val);
    }

    private static class DefaultCacheManager implements EntityCacheManager {
        @Override
        public <E extends Entity> E find(final Class<E> entityClass,
                                         final String key,
                                         final UpdateCacheHandler<E> updateCacheHandler) {
            return updateCacheHandler.updateCache(key);
        }

        @Override
        public <E extends Entity> void update(final Class<E> entityClass,
                                              final ObjectId entityId,
                                              final NotifyType notifyType) {
            // do nothing
        }

        @Override
        public <E extends Entity> void set(final String key, final E entity) {
            // do nothing
        }
    }
}
