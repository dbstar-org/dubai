package io.github.dbstarll.dubai.model.service.test;

import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.entity.test.InterfaceEntity;
import io.github.dbstarll.dubai.model.service.validation.EmptyValidation;
import io.github.dbstarll.dubai.model.service.validation.GeneralValidation;
import io.github.dbstarll.dubai.model.service.validation.Validation;

public final class FailedImplemental extends TestImplementals<InterfaceEntity, InterfaceService>
        implements FailedAttach {
    public FailedImplemental(InterfaceService service, boolean other) {
        super(service, null);
    }

    public FailedImplemental(boolean other, Collection<InterfaceEntity> collection) {
        super(null, collection);
    }

    public FailedImplemental(boolean other, InterfaceService service, Collection<InterfaceEntity> collection) {
        super(service, collection);
    }

    @Override
    public void failed() {
    }

    @GeneralValidation
    public Validation<InterfaceEntity> failedValidation() {
        return EmptyValidation.warp(entityClass);
    }
}
