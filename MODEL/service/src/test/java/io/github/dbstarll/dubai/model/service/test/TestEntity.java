package io.github.dbstarll.dubai.model.service.test;

import io.github.dbstarll.dubai.model.entity.Table;
import io.github.dbstarll.dubai.model.entity.func.Defunctable;
import io.github.dbstarll.dubai.model.entity.join.CompanyBase;

@Table
public interface TestEntity extends TestEntities, Defunctable, CompanyBase {

}
