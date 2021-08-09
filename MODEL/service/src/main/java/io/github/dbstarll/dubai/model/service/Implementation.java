package io.github.dbstarll.dubai.model.service;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target(TYPE)
@Retention(RUNTIME)
public @interface Implementation {
    /**
     * 获得{@link Implemental}实现类.
     *
     * @return {@link Implemental}实现类
     */
    Class<? extends Implemental> value();
}
