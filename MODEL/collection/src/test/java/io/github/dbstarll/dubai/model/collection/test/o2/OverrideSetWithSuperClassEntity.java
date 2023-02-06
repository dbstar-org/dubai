package io.github.dbstarll.dubai.model.collection.test.o2;

import io.github.dbstarll.dubai.model.entity.Table;

@Table
public interface OverrideSetWithSuperClassEntity extends ParentEntity {
    void setData(Object data);
}
