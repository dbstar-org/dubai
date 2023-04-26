package io.github.dbstarll.dubai.model.service;

import java.util.HashMap;
import java.util.Map;

public final class BeanMap {
    private final Map<Class<?>, Object> beans = new HashMap<>();

    public <T> void put(final Class<T> beanClass, final T bean) {
        beans.put(beanClass, bean);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(final Class<T> beanClass) {
        return (T) beans.computeIfAbsent(beanClass, c -> {
            throw new IllegalArgumentException("not found: " + c);
        });
    }
}
