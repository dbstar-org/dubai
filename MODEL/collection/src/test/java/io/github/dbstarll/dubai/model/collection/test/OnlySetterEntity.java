package io.github.dbstarll.dubai.model.collection.test;

import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.Table;

@Table
public interface OnlySetterEntity extends Entity {
    void setData(int data);
}
