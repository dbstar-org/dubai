package io.github.dbstarll.dubai.model.service.validation;

import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.service.validate.Validate;

public final class EmptyValidation<E extends Entity> extends AbstractValidation<E> {
    private EmptyValidation(final Class<E> entityClass) {
        super(entityClass);
    }

    @Override
    public void validate(final E entity, final E original, final Validate validate) {
    }

    /**
     * 封装一个什么都不做的空Validation.
     *
     * @param entityClass 实体类
     * @param <E>         实体类
     * @return EmptyValidation实例
     */
    public static <E extends Entity> EmptyValidation<E> warp(final Class<E> entityClass) {
        return new EmptyValidation<>(entityClass);
    }
}
