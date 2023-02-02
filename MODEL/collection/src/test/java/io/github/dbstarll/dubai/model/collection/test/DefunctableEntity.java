package io.github.dbstarll.dubai.model.collection.test;

import io.github.dbstarll.dubai.model.entity.Table;
import io.github.dbstarll.dubai.model.entity.func.Defunctable;

@Table
public interface DefunctableEntity extends SimpleEntity, Defunctable {

}
