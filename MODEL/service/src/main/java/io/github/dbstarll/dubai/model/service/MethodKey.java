package io.github.dbstarll.dubai.model.service;

import io.github.dbstarll.dubai.model.entity.Entity;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import static org.apache.commons.lang3.Validate.notNull;

class MethodKey {
    final String key;

    MethodKey(Method method, Class<?> entityClass) {
        this.key = toString(notNull(method), notNull(entityClass));
    }

    private static String toString(Method method, Class<?> entityClass) {
        return method.getName() + '(' + parameterToString(method, entityClass) + ')';
    }

    private static String parameterToString(Method method, Class<?> entityClass) {
        final StringBuilder sb = new StringBuilder();
        final Type[] parameterTypes = method.getGenericParameterTypes();
        for (int j = 0, length = parameterTypes.length; j < length; j++) {
            final Type type = parameterTypes[j];
            String param;
            if (type instanceof Class) {
                param = getTypeName((Class<?>) type);
            } else if (type instanceof TypeVariable && isEntityTypeVariable((TypeVariable<?>) type, entityClass)) {
                param = entityClass.getName();
            } else {
                param = type.toString();
            }
            if (method.isVarArgs() && j == length - 1) {
                // replace T[] with T...
                param = param.replaceFirst("\\[\\]$", "...");
            }
            sb.append(param);
            if (j < (length - 1)) {
                sb.append(',');
            }
        }
        return sb.toString();
    }

    private static boolean isEntityTypeVariable(TypeVariable<?> typeVariable, Class<?> entityClass) {
        for (Type bound : typeVariable.getBounds()) {
            if (!(bound instanceof Class)) {
                return false;
            }
            final Class<?> boundClass = (Class<?>) bound;
            if (!Entity.class.isAssignableFrom(boundClass)) {
                return false;
            } else if (!boundClass.isAssignableFrom(entityClass)) {
                return false;
            }
        }
        if (!(typeVariable.getGenericDeclaration() instanceof Class)) {
            return false;
        }
        return true;
    }

    private static String getTypeName(Class<?> type) {
        if (type.isArray()) {
            Class<?> cl = type;
            int dimensions = 0;
            while (cl.isArray()) {
                dimensions++;
                cl = cl.getComponentType();
            }
            StringBuilder sb = new StringBuilder();
            sb.append(cl.getName());
            for (int i = 0; i < dimensions; i++) {
                sb.append("[]");
            }
            return sb.toString();
        }
        return type.getName();
    }
}