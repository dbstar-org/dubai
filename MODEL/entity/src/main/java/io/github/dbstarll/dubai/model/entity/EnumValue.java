package io.github.dbstarll.dubai.model.entity;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface EnumValue {
    /**
     * 设置获取Enum持久化值的方法名称，默认为Enum.name().
     *
     * @return 获取Enum持久化值的方法名称
     */
    String method() default "name";
}
