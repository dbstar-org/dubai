package io.github.dbstarll.dubai.model.collection.test;

import io.github.dbstarll.dubai.model.entity.Entity;

public interface MethodGenericEntity<K, V> extends Entity {
    <S> void setData(S data);

    Object getData();
}
