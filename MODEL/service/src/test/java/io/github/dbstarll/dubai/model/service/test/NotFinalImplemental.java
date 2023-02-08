package io.github.dbstarll.dubai.model.service.test;

import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.entity.test.InterfaceEntity;
import io.github.dbstarll.dubai.model.service.validation.EmptyValidation;
import io.github.dbstarll.dubai.model.service.validation.GeneralValidation;
import io.github.dbstarll.dubai.model.service.validation.Validation;

public class NotFinalImplemental extends TestImplementals<InterfaceEntity, InterfaceService> implements NotFinalAttach {
    public NotFinalImplemental(InterfaceService service, Collection<InterfaceEntity> collection) {
        super(service, collection);
    }

    @Override
    public void notFinal() {
    }

    @GeneralValidation
    public Validation<InterfaceEntity> otherValidation() {
        return EmptyValidation.warp(entityClass);
    }
}
