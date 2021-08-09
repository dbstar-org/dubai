package io.github.dbstarll.dubai.model.service.test4;

import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.service.test.TestImplementals;
import io.github.dbstarll.dubai.model.service.validation.GeneralValidation;
import io.github.dbstarll.dubai.model.service.validation.GeneralValidation.Position;
import io.github.dbstarll.dubai.model.service.validation.Validation;

public final class TestValidServiceImplemental extends TestImplementals<TestValidEntity, TestValidService>
        implements TestValidServiceAttach {
    public TestValidServiceImplemental(TestValidService service, Collection<TestValidEntity> collection) {
        super(service, collection);
    }

    @GeneralValidation(position = Position.FIRST)
    public Validation<TestValidEntity> throwValidation() {
        throw new UnsupportedOperationException("throwValidation");
    }
}
