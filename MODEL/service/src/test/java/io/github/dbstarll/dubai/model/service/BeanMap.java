package io.github.dbstarll.dubai.model.service;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.Validate.notNull;

public final class BeanMap {
    private final Map<Class<?>, Object> beans = new HashMap<>();

    public <T> void put(final Class<T> beanClass, final T bean) {
        beans.put(notNull(beanClass, "beanClass not set"), notNull(bean, "bean not set"));
    }

    @SuppressWarnings("unchecked")
    public <T> T get(final Class<T> beanClass) {
        return (T) beans.computeIfAbsent(notNull(beanClass, "beanClass not set"), c -> {
            throw new IllegalArgumentException("not found: " + c);
        });
    }
}
