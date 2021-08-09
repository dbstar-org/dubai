package io.github.dbstarll.dubai.model.service.test3;

import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.service.test.TestImplementals;
import io.github.dbstarll.dubai.model.service.validate.Validate;
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

    private class MyDescriptionValidation extends DescriptionValidation {
        public MyDescriptionValidation() {
            super(-1);
        }
    }
}
