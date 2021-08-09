package io.github.dbstarll.dubai.model.collection;

import io.github.dbstarll.dubai.model.entity.Entity;

public interface CollectionNameGenerator {
    <E extends Entity> String generateCollectionName(Class<E> entityClass) throws CollectionInitializeException;
}
