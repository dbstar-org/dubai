package io.github.dbstarll.dubai.model.service.test;

import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.test.ClassEntity;
import io.github.dbstarll.dubai.model.entity.test.InterfaceEntity;
import io.github.dbstarll.dubai.model.service.validation.EmptyValidation;
import io.github.dbstarll.dubai.model.service.validation.GeneralValidation;
import io.github.dbstarll.dubai.model.service.validation.GeneralValidation.Position;
import io.github.dbstarll.dubai.model.service.validation.Validation;
import org.bson.types.ObjectId;

public final class InterfaceServiceImplemental extends TestImplementals<InterfaceEntity, InterfaceService>
        implements InterfaceServiceAttach {
    public InterfaceServiceImplemental(InterfaceService service, Collection<InterfaceEntity> collection) {
        super(service, collection);
    }

    @Override
    public boolean contains(ObjectId id) {
        return true;
    }

    @Override
    public void throwException() throws Exception {
        throw new IllegalAccessException("throwException");
    }

    @Override
    public <T> T call(T value) {
        return null;
    }

    @Override
    public String[] call(String... values) {
        return null;
    }

    @Override
    public String[] call(boolean bol, String... values) {
        return null;
    }

    @Override
    public <T extends Entity> T call1(T value) {
        return null;
    }

    @Override
    public void call2(Collection<? extends Entity> value) {
    }

    @Override
    public <T extends ClassEntity> T call3(T value) {
        return null;
    }

    @Override
    public <T extends Collection<? extends Entity>> T call4(T value) {
        return null;
    }

    @GeneralValidation
    public String stringValidation() {
        return null;
    }

    @GeneralValidation
    public Validation<InterfaceEntity> nullValidation() {
        return null;
    }

    @GeneralValidation
    public Validation<InterfaceEntity> emptyValidation() {
        return EmptyValidation.warp(entityClass);
    }

    @GeneralValidation
    public Validation<InterfaceEntity> defunctValidation() {
        return EmptyValidation.warp(entityClass);
    }

    @GeneralValidation(position = Position.LAST)
    public Validation<InterfaceEntity> lastValidation() {
        return EmptyValidation.warp(entityClass);
    }
}
