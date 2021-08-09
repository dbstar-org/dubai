package io.github.dbstarll.dubai.model.service.validation;

import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.service.validate.Validate;

public final class EmptyValidation<E extends Entity> extends AbstractValidation<E> {
    private EmptyValidation(Class<E> entityClass) {
        super(entityClass);
    }

    @Override
    public void validate(E entity, E original, Validate validate) {
    }

    public static <E extends Entity> EmptyValidation<E> warp(Class<E> entityClass) {
        return new EmptyValidation<>(entityClass);
    }
}
