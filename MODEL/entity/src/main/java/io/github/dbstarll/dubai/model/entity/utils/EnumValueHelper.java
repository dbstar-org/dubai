package io.github.dbstarll.dubai.model.entity.utils;

import io.github.dbstarll.dubai.model.entity.EnumValue;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.commons.lang3.Validate.notBlank;

public final class EnumValueHelper<T extends Enum<T>> {
    private static final String DEFAULT_METHOD_NAME = "name";

    private final String enumType;
    private final Method method;
    private final Map<String, T> names = new ConcurrentHashMap<>();

    /**
     * Construct an instance for teh given enum class.
     *
     * @param enumType the enum class
     */
    public EnumValueHelper(final Class<T> enumType) {
        this.enumType = enumType.getName();
        this.method = parseMethod(enumType);
        Arrays.stream(enumType.getEnumConstants()).forEach(e -> names.compute(name(e), (name, exist) -> {
            if (exist == null) {
                return e;
            } else {
                throw new IllegalArgumentException(
                        String.format("duplicate name [%s] for [%s] and [%s]: %s", name, exist, e, this.enumType));
            }
        }));
    }

    private static String parseMethodName(final Class<?> clazz) {
        final EnumValue enumValue = clazz.getAnnotation(EnumValue.class);
        if (enumValue != null) {
            if (StringUtils.isBlank(enumValue.method())) {
                throw new IllegalArgumentException("@EnumValue.method() not set: " + clazz.getName());
            } else {
                return enumValue.method();
            }
        } else {
            return DEFAULT_METHOD_NAME;
        }
    }

    private static Method parseMethod(final Class<?> clazz) {
        if (!Modifier.isPublic(clazz.getModifiers())) {
            throw new IllegalArgumentException(String.format("enumType must be public: %s", clazz.getName()));
        }

        final String methodName = parseMethodName(clazz);
        final Method method;
        try {
            method = clazz.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(
                    String.format("get method[%s] failed: %s", methodName, clazz.getName()), e);
        }

        // check return type
        if (String.class != method.getReturnType()) {
            throw new IllegalArgumentException(
                    String.format("method[%s]'s returnType must be String: %s", methodName, clazz.getName()));
        }

        // check modifier
        final int modifiers = method.getModifiers();
        if (Modifier.isStatic(modifiers)) {
            throw new IllegalArgumentException(
                    String.format("method[%s] must not be static: %s", methodName, clazz.getName()));
        }

        return method;
    }

    /**
     * Returns the name of this enum constant, exactly as declared in its enum declaration.
     *
     * @param enumValue enum constant
     * @return the name of this enum constant
     */
    public String name(final T enumValue) {
        try {
            return (String) method.invoke(enumValue);
        } catch (InvocationTargetException ex) {
            throw new IllegalStateException(
                    String.format("get enum[%s]'s name failed: %s", enumValue, enumType), ex.getCause());
        } catch (Exception ex) {
            throw new IllegalStateException(
                    String.format("get enum[%s]'s name failed: %s", enumValue, enumType), ex);
        }
    }

    /**
     * Returns the enum constant with the specified name.  The name must match exactly an identifier used
     * to declare an enum constant in this type.
     *
     * @param name the name of the constant to return
     * @return the enum constant with the specified name
     */
    public T valueOf(final String name) {
        return names.computeIfAbsent(notBlank(name, "name is blank"), n -> {
            throw new IllegalArgumentException(String.format("No enum constant[%s]: %s", name, enumType));
        });
    }
}
