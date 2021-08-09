package io.github.dbstarll.dubai.model.cache;

import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.notify.NotifyType;
import org.bson.types.ObjectId;

public interface EntityCacheManager {
    <E extends Entity> E find(Class<E> entityClass, String key, UpdateCacheHandler<E> updateCacheHandler);

    <E extends Entity> void update(Class<E> entityClass, ObjectId entityId, NotifyType notifyType);

    <E extends Entity> void set(String key, E entity);

    interface UpdateCacheHandler<E extends Entity> {
        E updateCache(String key);
    }
}