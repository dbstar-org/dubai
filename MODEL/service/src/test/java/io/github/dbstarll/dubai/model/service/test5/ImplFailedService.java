package io.github.dbstarll.dubai.model.service.test5;

import io.github.dbstarll.dubai.model.service.EntityService;
import io.github.dbstarll.dubai.model.service.test.TestServices;

@EntityService
public interface ImplFailedService extends TestServices<ImplFailedEntity>, ImplFailedAttach {
}
