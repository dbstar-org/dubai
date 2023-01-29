package io.github.dbstarll.dubai.model.cache;

import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.notify.NotifyType;
import org.bson.types.ObjectId;

public interface EntityCacheManager {
    /**
     * 从缓存中查找实体对象，如果在缓存中没有找到实体时，从缓存更新处理器中获得最新的实体并缓存.
     *
     * @param entityClass        实体类
     * @param key                缓存key
     * @param updateCacheHandler 缓存更新处理器
     * @param <E>                实体类
     * @return key对应的缓存的实体对象
     */
    <E extends Entity> E find(Class<E> entityClass, String key, UpdateCacheHandler<E> updateCacheHandler);

    /**
     * 根据NotifyType来更新缓存中的实体.
     *
     * @param entityClass 实体类
     * @param entityId    实体ID
     * @param notifyType  通知类型
     * @param <E>         实体类
     */
    <E extends Entity> void update(Class<E> entityClass, ObjectId entityId, NotifyType notifyType);

    /**
     * @param key
     * @param entity
     * @param <E>
     */
    <E extends Entity> void set(String key, E entity);

    interface UpdateCacheHandler<E extends Entity> {
        E updateCache(String key);
    }
}
