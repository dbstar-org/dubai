package io.github.dbstarll.dubai.model.collection.test;

import io.github.dbstarll.dubai.model.entity.Table;

@Table
public interface InheritMethodGenericHidingEntity extends MethodGenericHidingEntity<String, Integer> {
}
