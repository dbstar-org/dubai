package io.github.dbstarll.dubai.model.service.test3.contentable;

import io.github.dbstarll.dubai.model.service.EntityService;
import io.github.dbstarll.dubai.model.service.attach.ContentAttach;
import io.github.dbstarll.dubai.model.service.test.TestServices;

@EntityService
public interface TestContentableService
        extends TestServices<TestContentableEntity>, ContentAttach {
}
