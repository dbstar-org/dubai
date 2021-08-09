package io.github.dbstarll.dubai.model.collection.test;

import io.github.dbstarll.dubai.model.entity.Table;

@Table
public interface SimpleGenericEntity extends GenericEntity<String, Integer> {
    @Override
    String getKey();

    @Override
    void setKey(String key);
}
