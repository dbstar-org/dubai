package io.github.dbstarll.dubai.model.collection.test.o2;

import io.github.dbstarll.dubai.model.entity.Table;

@Table
public interface OverrideSetWithSubClassEntity extends ParentEntity {
    void setData(Integer data);
}
