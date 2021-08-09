package io.github.dbstarll.dubai.model.service.test3.namable;

import io.github.dbstarll.dubai.model.service.EntityService;
import io.github.dbstarll.dubai.model.service.test.TestServices;

@EntityService
public interface TestNamableService extends TestServices<TestNamableEntity>, TestNamableServiceAttach {
}
