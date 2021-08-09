package io.github.dbstarll.dubai.model.entity;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface Table {
    /**
     * 数据表名.
     *
     * @return 数据表名
     */
    String value() default "";
}
