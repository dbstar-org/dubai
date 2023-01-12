package io.github.dbstarll.dubai.model.collection;

import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.Namespace;
import io.github.dbstarll.dubai.model.entity.Table;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;

public class AnnotationCollectionNameGenerator implements CollectionNameGenerator {
    @Override
    public final <E extends Entity> String generateCollectionName(final Class<E> entityClass)
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

    private <A extends Annotation> A getAnnotation(final Class<?> entityClass, final Class<A> annotationClass) {
        final A a = entityClass.getAnnotation(annotationClass);
        if (a != null) {
            return a;
        }

        if (entityClass.getSuperclass() != null && Entity.class.isAssignableFrom(entityClass.getSuperclass())) {
            final A b = getAnnotation(entityClass.getSuperclass(), annotationClass);
            if (b != null) {
                return b;
            }
        }
        for (Class<?> i : entityClass.getInterfaces()) {
            if (Entity.class.isAssignableFrom(i)) {
                final A b = getAnnotation(i, annotationClass);
                if (b != null) {
                    return b;
                }
            }
        }

        return null;
    }

    private <E extends Entity> String generateNamespace(final Class<E> entityClass, final Namespace namespace)
            throws CollectionInitializeException {
        if (namespace != null) {
            final String annotationNamespace = generateNamespace(namespace);
            if (StringUtils.isNotBlank(annotationNamespace)) {
                return annotationNamespace;
            }
        }
        return generateNamespace(entityClass);
    }

    /**
     * 根据namespace注解来生成namespace.
     *
     * @param namespace namespace注解
     * @return 生成的namespace
     * @throws CollectionInitializeException 集合初始化异常
     */
    protected String generateNamespace(final Namespace namespace) throws CollectionInitializeException {
        return namespace.value();
    }

    /**
     * 根据实体类来生成namespace.
     *
     * @param entityClass 实体类
     * @param <E>         实体类的类型
     * @return 生成的namespace
     * @throws CollectionInitializeException 集合初始化异常
     */
    protected <E extends Entity> String generateNamespace(final Class<E> entityClass)
            throws CollectionInitializeException {
        return null;
    }

    private <E extends Entity> String generateName(final Class<E> entityClass, final Table table)
            throws CollectionInitializeException {
        final String annotationName = generateName(table);
        if (StringUtils.isNotBlank(annotationName)) {
            return annotationName;
        } else {
            return generateName(entityClass);
        }
    }

    /**
     * 根据table注解来生成表名.
     *
     * @param table table注解
     * @return 生成的表名
     * @throws CollectionInitializeException 集合初始化异常
     */
    protected String generateName(final Table table) throws CollectionInitializeException {
        return table.value();
    }

    /**
     * 根据实体类来生成表名.
     *
     * @param entityClass 实体类
     * @param <E>         实体类的类型
     * @return 生成的表名
     * @throws CollectionInitializeException 集合初始化异常
     */
    protected <E extends Entity> String generateName(final Class<E> entityClass) throws CollectionInitializeException {
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
