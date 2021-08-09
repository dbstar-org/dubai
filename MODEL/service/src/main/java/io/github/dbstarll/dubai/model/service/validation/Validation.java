package io.github.dbstarll.dubai.model.service.validation;

import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.service.validate.Validate;

public interface Validation<E extends Entity> {
    /**
     * 对实体进行校验，将不合规的条目填充到{@link Validate}.
     *
     * @param entity   待校验的实体
     * @param original 实体原件
     * @param validate 校验结果
     */
    void validate(E entity, E original, Validate validate);
}
