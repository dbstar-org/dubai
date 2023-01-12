package io.github.dbstarll.dubai.model.service.validation;

import io.github.dbstarll.dubai.model.entity.Entity;

import static org.apache.commons.lang3.Validate.notNull;

public abstract class AbstractValidation<E extends Entity> implements Validation<E> {
    protected final Class<E> entityClass;

    protected AbstractValidation(final Class<E> entityClass) {
        this.entityClass = notNull(entityClass, "entityClass is null");
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + getClass().getName().hashCode();
        result = prime * result + entityClass.getName().hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return entityClass == ((AbstractValidation<?>) obj).entityClass;
    }

    @Override
    public String toString() {
        return getClass().getName() + "<" + entityClass.getName() + ">";
    }
}
