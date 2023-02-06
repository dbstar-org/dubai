package io.github.dbstarll.dubai.model.service.test3;

import io.github.dbstarll.dubai.model.service.Implementation;
import io.github.dbstarll.dubai.model.service.ServiceDeleter;
import io.github.dbstarll.dubai.model.service.ServiceSaver;
import io.github.dbstarll.dubai.model.service.test.TestAttachs;
import io.github.dbstarll.dubai.model.service.validate.Validate;
import io.github.dbstarll.dubai.model.service.validate.ValidateException;

@Implementation(TestServiceImplemental.class)
public interface TestServiceAttach extends TestAttachs, ServiceSaver<TestEntity>, ServiceDeleter<TestEntity> {
    TestEntity saveFailed(TestEntity entity, Validate validate) throws ValidateException;
}
