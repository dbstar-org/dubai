package io.github.dbstarll.dubai.model.service.validation;

import io.github.dbstarll.dubai.model.entity.Base;
import io.github.dbstarll.dubai.model.entity.Entity;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface GeneralValidation {
    enum Position {
        FIRST, PRE, POST, LAST
    }

    /**
     * 获得启用此校验的{@link Base}Entity基准类.
     *
     * @return {@link Base}Entity基准类
     */
    Class<? extends Base> value() default Entity.class;

    /**
     * 设置校验的位置顺序.
     *
     * @return 校验的位置顺序
     */
    Position position() default Position.PRE;
}
