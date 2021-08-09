package io.github.dbstarll.dubai.model.collection;

import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.Namespace;
import io.github.dbstarll.dubai.model.entity.Table;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;

public class AnnotationCollectionNameGenerator implements CollectionNameGenerator {
    @Override
    public final <E extends Entity> String generateCollectionName(Class<E> entityClass)
            throws CollectionInitializeException {
        final Table table = entityClass.getAnnotation(Table.class);
        if (null == table) {
            throw new CollectionInitializeException("Table annotation not find on entity class: " + entityClass);
        }

        final String namespace = generateNamespace(entityClass, getAnnotation(entityClass, Namespace.class));
        final String name = generateName(entityClass, table);
        if (StringUtils.isBlank(namespace)) {
            return name;
        } else if (namespace.endsWith("_")) {
            return namespace + name;
        } else {
            return namespace + '_' + name;
        }
    }

    private <A extends Annotation> A getAnnotation(Class<?> entityClass, Class<A> annotationClass) {
        final A a;
        if ((a = entityClass.getAnnotation(annotationClass)) != null) {
            return a;
        }

        if (entityClass.getSuperclass() != null && Entity.class.isAssignableFrom(entityClass.getSuperclass())) {
            final A b;
            if ((b = getAnnotation(entityClass.getSuperclass(), annotationClass)) != null) {
                return b;
            }
        }
        for (Class<?> i : entityClass.getInterfaces()) {
            if (Entity.class.isAssignableFrom(i)) {
                final A b;
                if ((b = getAnnotation(i, annotationClass)) != null) {
                    return b;
                }
            }
        }

        return null;
    }

    private <E extends Entity> String generateNamespace(Class<E> entityClass, Namespace namespace)
            throws CollectionInitializeException {
        final String annotationNamespace;
        if (namespace == null || StringUtils.isBlank(annotationNamespace = generateNamespace(namespace))) {
            return generateNamespace(entityClass);
        } else {
            return annotationNamespace;
        }
    }

    protected String generateNamespace(Namespace namespace) throws CollectionInitializeException {
        return namespace.value();
    }

    protected <E extends Entity> String generateNamespace(Class<E> entityClass) throws CollectionInitializeException {
        return null;
    }

    private <E extends Entity> String generateName(Class<E> entityClass, Table table)
            throws CollectionInitializeException {
        final String annotationName = generateName(table);
        if (StringUtils.isNotBlank(annotationName)) {
            return annotationName;
        } else {
            return generateName(entityClass);
        }
    }

    protected String generateName(Table table) throws CollectionInitializeException {
        return table.value();
    }

    protected <E extends Entity> String generateName(Class<E> entityClass) throws CollectionInitializeException {
        final StringBuilder builder = new StringBuilder();
        for (char ch : entityClass.getSimpleName().toCharArray()) {
            if (Character.isUpperCase(ch)) {
                if (builder.length() != 0) {
                    builder.append('_');
                }
                builder.append(Character.toLowerCase(ch));
            } else {
                builder.append(ch);
            }
        }
        return builder.toString();
    }
}
