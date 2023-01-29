package io.github.dbstarll.dubai.model.service.test3;

import io.github.dbstarll.dubai.model.service.EntityService;
import io.github.dbstarll.dubai.model.service.attach.DefunctAttach;
import io.github.dbstarll.dubai.model.service.attach.SourceAttach;
import io.github.dbstarll.dubai.model.service.test.TestServices;

@EntityService
public interface TestService extends TestServices<TestEntity>, DefunctAttach<TestEntity>, SourceAttach,
        DelayAttach, TestServiceAttach {
}
