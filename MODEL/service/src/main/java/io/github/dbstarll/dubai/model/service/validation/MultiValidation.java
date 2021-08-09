package io.github.dbstarll.dubai.model.service.validation;

import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.service.validate.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.apache.commons.lang3.Validate.noNullElements;
import static org.apache.commons.lang3.Validate.notNull;

public final class MultiValidation<E extends Entity> extends AbstractValidation<E> implements Iterable<Validation<E>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiValidation.class);

    private final List<Validation<E>> validations;

    /**
     * 构建MultiValidation.
     *
     * @param entityClass 实体类
     * @param validations 从属的Validation
     */
    @SafeVarargs
    public MultiValidation(Class<E> entityClass, Validation<E>... validations) {
        super(entityClass);
        this.validations = new LinkedList<>();
        addValidation(validations);
    }

    /**
     * 添加从属的Validation.
     *
     * @param validations 从属的Validation
     */
    @SafeVarargs
    public final void addValidation(Validation<E>... validations) {
        noNullElements(notNull(validations, "validations is null"), "validations contains null element at index: %d");
        for (Validation<E> validation : validations) {
            if (validation instanceof MultiValidation) {
                for (Validation<E> sub : (MultiValidation<E>) validation) {
                    addValidation(sub);
                }
            } else {
                if (!this.validations.contains(validation)) {
                    this.validations.add(validation);
                }
            }
        }
    }

    public int size() {
        return validations.size();
    }

    public boolean isEmpty() {
        return validations.isEmpty();
    }

    @Override
    public Iterator<Validation<E>> iterator() {
        return validations.iterator();
    }

    @Override
    public void validate(E entity, E original, Validate validate) {
        for (Validation<E> validation : validations) {
            validation.validate(entity, original, validate);
            LOGGER.debug("validate: <{}>{}", entityClass.getName(), validation.getClass().getName());
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + validations.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        return validations.equals(((MultiValidation<?>) obj).validations);
    }

    @Override
    public String toString() {
        return super.toString() + " [validations=" + validations.size() + "]";
    }
}
