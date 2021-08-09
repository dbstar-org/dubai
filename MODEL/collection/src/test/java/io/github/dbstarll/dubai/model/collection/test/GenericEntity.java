package io.github.dbstarll.dubai.model.collection.test;

import io.github.dbstarll.dubai.model.entity.Entity;

import java.util.Map;

public interface GenericEntity<K, V> extends Entity {
    K getKey();

    void setKey(K key);

    V getValue();

    void setValue(V value);

    Map<K, Map<K, Map<K, V>>> getMap();

    void setMap(Map<K, Map<K, Map<K, V>>> data);

    int getInt();

    void setInt(int value);
}
