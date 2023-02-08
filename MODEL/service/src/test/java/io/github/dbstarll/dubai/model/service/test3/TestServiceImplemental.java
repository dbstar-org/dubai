package io.github.dbstarll.dubai.model.service.test3;

import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.service.test.TestImplementals;
import io.github.dbstarll.dubai.model.service.validate.Validate;
import io.github.dbstarll.dubai.model.service.validate.ValidateException;
import io.github.dbstarll.dubai.model.service.validation.EmptyValidation;
import io.github.dbstarll.dubai.model.service.validation.GeneralValidation;
import io.github.dbstarll.dubai.model.service.validation.Validation;
import org.bson.types.ObjectId;

public final class TestServiceImplemental extends TestImplementals<TestEntity, TestService>
        implements TestServiceAttach {
    public TestServiceImplemental(TestService service, Collection<TestEntity> collection) {
        super(service, collection);
    }

    @Override
    public TestEntity save(TestEntity entity, ObjectId newEntityId, Validate validate) {
        return validateAndSave(entity, newEntityId, validate, new MyDescriptionValidation());
    }

    @Override
    public TestEntity deleteById(ObjectId id, Validate validate) throws ValidateException {
        return validateAndDelete(id, validate);
    }

    @Override
    public TestEntity saveFailed(TestEntity entity, Validate validate) throws ValidateException {
        return validateAndSave(entity, null, validate, new FailedValidation());
    }

    private class MyDescriptionValidation extends DescriptionValidation {
        public MyDescriptionValidation() {
            super(-1);
        }
    }

    private static class FailedValidation implements Validation<TestEntity> {
        @Override
        public void validate(TestEntity entity, TestEntity original, Validate validate) {
            validate.addActionError("SaveFailed");
        }
    }

    @GeneralValidation
    public Validation<TestEntity> emptyValidation() {
        return EmptyValidation.warp(entityClass);
    }
}
