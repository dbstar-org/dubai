package io.github.dbstarll.dubai.model.collection;

import io.github.dbstarll.dubai.model.entity.Entity;

public interface CollectionNameGenerator {
    /**
     * 根据实体类来生成集合的名称.
     *
     * @param entityClass 实体类
     * @param <E>         实体类
     * @return 集合的名称
     * @throws CollectionInitializeException 集合初始化异常时抛出
     */
    <E extends Entity> String generateCollectionName(Class<E> entityClass) throws CollectionInitializeException;
}
