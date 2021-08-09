package io.github.dbstarll.dubai.model.service.test3;

import io.github.dbstarll.dubai.model.entity.Table;
import io.github.dbstarll.dubai.model.entity.func.Defunctable;
import io.github.dbstarll.dubai.model.entity.info.Describable;
import io.github.dbstarll.dubai.model.entity.info.Sourceable;
import io.github.dbstarll.dubai.model.service.test.TestEntities;

@Table
public interface TestEntity extends TestEntities, Defunctable, Describable, Sourceable {

}
