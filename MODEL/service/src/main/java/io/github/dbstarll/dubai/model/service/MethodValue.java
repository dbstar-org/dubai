package io.github.dbstarll.dubai.model.service;

import java.lang.reflect.Method;

import static org.apache.commons.lang3.Validate.notNull;

class MethodValue {
    final Class<?> key;
    final Method value;

    MethodValue(Class<?> key, Method value) {
        this.key = notNull(key);
        this.value = notNull(value);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + value.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MethodValue other = (MethodValue) obj;
        return value.equals(other.value);
    }
}