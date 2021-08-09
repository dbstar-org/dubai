package io.github.dbstarll.dubai.model.collection.test;

import io.github.dbstarll.dubai.model.entity.Table;
import io.github.dbstarll.dubai.model.entity.func.Cacheable;

@Table
public interface CacheableEntity extends NotifiableEntity, Cacheable {

}
