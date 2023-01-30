package io.github.dbstarll.dubai.model.collection;

import com.mongodb.client.MongoDatabase;
import io.github.dbstarll.dubai.model.cache.EntityCacheManager;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.entity.func.Cacheable;
import io.github.dbstarll.dubai.model.entity.func.Defunctable;
import io.github.dbstarll.dubai.model.entity.func.Notifiable;
import io.github.dbstarll.dubai.model.notify.NotifyProvider;

public final class CollectionFactory {
    private final MongoDatabase mongoDatabase;
    private NotifyProvider notifyProvider;
    private EntityCacheManager entityCacheManager;
    private CollectionNameGenerator collectionNameGenerator = new AnnotationCollectionNameGenerator();

    /**
     * 构造CollectionFactory.
     *
     * @param mongoDatabase MongoDatabase实例
     */
    public CollectionFactory(final MongoDatabase mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
    }

    /**
     * 设置NotifyProvider.
     *
     * @param notifyProvider NotifyProvider
     */
    public void setNotifyProvider(final NotifyProvider notifyProvider) {
        this.notifyProvider = notifyProvider;
    }

    /**
     * 设置EntityCacheManager.
     *
     * @param entityCacheManager EntityCacheManager
     */
    public void setEntityCacheManager(final EntityCacheManager entityCacheManager) {
        this.entityCacheManager = entityCacheManager;
    }

    /**
     * 设置CollectionNameGenerator.
     *
     * @param collectionNameGenerator CollectionNameGenerator
     */
    public void setCollectionNameGenerator(final CollectionNameGenerator collectionNameGenerator) {
        this.collectionNameGenerator = collectionNameGenerator;
    }

    /**
     * 根据提供的实体类来构造相应的Collection.
     *
     * @param entityClass 实体类
     * @param <E>         实体类
     * @return Collection of entityClass
     * @throws CollectionInitializeException 当entityClass不是一个有效的实体类时抛出
     */
    public <E extends Entity> Collection<E> newInstance(final Class<E> entityClass)
            throws CollectionInitializeException {
        if (EntityFactory.isEntityClass(entityClass)) {
            Collection<E> collection = buildBaseCollection(entityClass);
            if (Cacheable.class.isAssignableFrom(entityClass)) {
                collection = buildCacheableCollection(collection);
            } else if (Notifiable.class.isAssignableFrom(entityClass)) {
                collection = buildNotifiableCollection(collection);
            }
            if (Defunctable.class.isAssignableFrom(entityClass)) {
                collection = buildDefunctableCollection(collection);
            }
            return collection;
        } else {
            throw new CollectionInitializeException("Invalid EntityClass: " + entityClass);
        }
    }

    private <E extends Entity> Collection<E> buildBaseCollection(final Class<E> entityClass)
            throws CollectionInitializeException {
        final String collectionName = collectionNameGenerator.generateCollectionName(entityClass);
        return new BaseCollection<>(mongoDatabase.getCollection(collectionName, entityClass));
    }

    private <E extends Entity> CacheableCollection<E> buildCacheableCollection(final Collection<E> base) {
        final CacheableCollection<E> collection = new CacheableCollection<>(base);
        if (entityCacheManager != null) {
            collection.setEntityCacheManager(entityCacheManager);
        }
        if (notifyProvider != null) {
            collection.setNotifyProvider(notifyProvider);
        }
        return collection;
    }

    private <E extends Entity> NotifiableCollection<E> buildNotifiableCollection(final Collection<E> base) {
        final NotifiableCollection<E> collection = new NotifiableCollection<>(base);
        if (notifyProvider != null) {
            collection.setNotifyProvider(notifyProvider);
        }
        return collection;
    }

    private <E extends Entity> DefunctableCollection<E> buildDefunctableCollection(final Collection<E> base) {
        return new DefunctableCollection<>(base);
    }
}
