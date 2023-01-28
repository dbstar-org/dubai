package io.github.dbstarll.dubai.model.collection.test;

import io.github.dbstarll.dubai.model.entity.Entity;

public interface MethodGenericHidingEntity<K, V> extends Entity {
    <K> void setData(K data);

    Object getData();
}
