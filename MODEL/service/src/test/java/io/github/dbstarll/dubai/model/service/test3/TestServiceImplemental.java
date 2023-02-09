package io.github.dbstarll.dubai.model.service.test3;

import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.entity.info.Describable;
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
    public TestEntity saveFailed(TestEntity entity, Validate validate) throws ValidateException {
        return validateAndSave(entity, null, validate, new FailedValidation());
    }

    @Override
    public TestEntity saveException(TestEntity entity, Validate validate) throws ValidateException {
        return validateAndSave(entity, null, validate, new ExceptionValidation());
    }

    @Override
    public TestEntity deleteById(ObjectId id, Validate validate) throws ValidateException {
        return validateAndDelete(id, validate);
    }

    @Override
    public TestEntity deleteByIdFailed(ObjectId id, Validate validate) throws ValidateException {
        return validateAndDelete(id, validate, new FailedValidation());
    }

    @Override
    public TestEntity deleteByIdException(ObjectId id, Validate validate) throws ValidateException {
        return validateAndDelete(id, validate, new ExceptionValidation());
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

    private static class ExceptionValidation implements Validation<TestEntity> {
        @Override
        public void validate(TestEntity entity, TestEntity original, Validate validate) {
            throw new IllegalStateException("SaveException");
        }
    }

    @GeneralValidation
    public Validation<TestEntity> emptyValidation() {
        return EmptyValidation.warp(entityClass);
    }

    /**
     * 覆盖默认的描述校验.
     *
     * @return DescriptionValidation
     */
    @GeneralValidation(Describable.class)
    public Validation<TestEntity> myDescriptionValidation() {
        return new DescriptionValidation(60);
    }
}
