package io.github.dbstarll.dubai.model.collection.test;

import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.Table;

@Table
public interface DirectMethodGenericEntity extends Entity {
    <S> void setData(S data);

    Object getData();
}
