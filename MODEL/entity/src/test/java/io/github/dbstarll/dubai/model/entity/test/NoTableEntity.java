package io.github.dbstarll.dubai.model.entity.test;

import io.github.dbstarll.dubai.model.entity.Entity;

public interface NoTableEntity extends Entity {
    boolean isBooleanFromNoTableEntity();

    void setBooleanFromNoTableEntity(boolean booleanFromNoTableEntity);
}
