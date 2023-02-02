package io.github.dbstarll.dubai.model.collection.test.o2;

import io.github.dbstarll.dubai.model.entity.Entity;

public interface ParentEntity extends Entity {
    void setData(Number data);

    Number getData();
}
