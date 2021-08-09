package io.github.dbstarll.dubai.model.service.test3.namable;

import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.service.test.TestImplementals;
import io.github.dbstarll.dubai.model.service.validate.Validate;
import org.bson.types.ObjectId;

public final class TestNamableServiceImplemental extends TestImplementals<TestNamableEntity, TestNamableService>
        implements TestNamableServiceAttach {
    public TestNamableServiceImplemental(TestNamableService service, Collection<TestNamableEntity> collection) {
        super(service, collection);
    }

    @Override
    public TestNamableEntity save(TestNamableEntity entity, ObjectId newEntityId, Validate validate) {
        return validateAndSave(entity, newEntityId, validate, new MyNameValidation());
    }

    private class MyNameValidation extends NameValidation {
        public MyNameValidation() {
            super(-1, -1);
        }
    }

    @Override
    public void name(int minLength, int maxLength) {
        new NameValidation(minLength, maxLength);
    }
}
