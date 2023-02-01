package org.bson.codecs.pojo;

import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.entity.EntityModifier;
import io.github.dbstarll.dubai.model.mongodb.EntityInstanceCreatorFactory;
import org.bson.codecs.configuration.CodecConfigurationException;
import org.bson.codecs.pojo.PropertyReflectionUtils.PropertyMethods;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Collections.reverse;
import static org.bson.assertions.Assertions.notNull;
import static org.bson.codecs.pojo.PropertyReflectionUtils.getPropertyMethods;
import static org.bson.codecs.pojo.PropertyReflectionUtils.isGetter;
import static org.bson.codecs.pojo.PropertyReflectionUtils.toPropertyName;

public final class EntityConvention implements Convention {
    @Override
    public void apply(final ClassModelBuilder<?> classModelBuilder) {
        process(classModelBuilder);
    }

    @SuppressWarnings("unchecked")
    private <T> void process(final ClassModelBuilder<T> classModelBuilder) {
        final Class<T> entityClass = classModelBuilder.getType();
        if (EntityFactory.isEntityInterface(entityClass)) {
            processEntity((ClassModelBuilder<? extends Entity>) classModelBuilder, false);
        } else if (EntityFactory.isEntityProxy(entityClass)) {
            processEntity((ClassModelBuilder<? extends Entity>) classModelBuilder, true);
        }
    }

    private <T extends Entity> void processEntity(final ClassModelBuilder<T> classModelBuilder, final boolean proxy) {
        final Class<T> entityClass;
        if (proxy) {
            entityClass = EntityFactory.getEntityClass(classModelBuilder.getType());
        } else {
            entityClass = classModelBuilder.getType();
        }
        classModelBuilder.instanceCreatorFactory(new EntityInstanceCreatorFactory<>(entityClass));

        final Context context = new Context();
        collectMethods(EntityModifier.class, context, null);
        collectMethods(entityClass, context, null);
        context.process(classModelBuilder);
    }

    private static <T, S> void collectMethods(final Class<T> entityInterface, final Context context,
                                              final TypeData<S> parentClassTypeData) {
        final String declaringClassName = entityInterface.toString();

        context.annotations.addAll(Arrays.asList(entityInterface.getDeclaredAnnotations()));

        final List<String> genericTypeNames = Arrays.stream(entityInterface.getTypeParameters())
                .map(TypeVariable::getName).collect(Collectors.toList());

        final PropertyMethods propertyMethods = getPropertyMethods(entityInterface);
        // Note that we're processing setters before getters. It's typical for setters to have more general types
        // than getters (e.g.: getter returning ImmutableList, but setter accepting Collection), so by evaluating
        // setters first, we'll initialize the PropertyMetadata with the more general type
        propertyMethods.getSetterMethods().stream().map(method -> collectMethod(declaringClassName, context,
                parentClassTypeData, genericTypeNames, method, false)).forEach(context.propertyNames::add);
        propertyMethods.getGetterMethods().stream().map(method -> collectMethod(declaringClassName, context,
                parentClassTypeData, genericTypeNames, method, true)).forEach(context.propertyNames::add);

        for (Type t : entityInterface.getGenericInterfaces()) {
            final TypeData<T> classTypeData = TypeData.newInstance(t, entityInterface);
            if (t instanceof ParameterizedType) {
                t = ((ParameterizedType) t).getRawType();
            }
            collectMethods((Class<?>) t, context, classTypeData);
        }
    }

    private static String collectMethod(final String declaringClassName, final Context context,
                                        final TypeData<?> parentClassTypeData, final List<String> genericTypeNames,
                                        final Method method, final boolean getter) {
        final String propertyName = toPropertyName(method);

        if (getter) {
            // If the getter is overridden in a subclass, we only want to process that property, and ignore
            // potentially less specific methods from super classes
            final PropertyMetadata<?> existPropertyMetadata = context.propertyNameMap.get(propertyName);
            if (existPropertyMetadata != null && existPropertyMetadata.getGetter() != null) {
                return propertyName;
            }
        }

        final PropertyMetadata<?> propertyMetadata = getOrCreateMethodPropertyMetadata(propertyName, declaringClassName,
                context, TypeData.newInstance(method));
        cachePropertyTypeData(propertyMetadata, context, parentClassTypeData, genericTypeNames, getGenericType(method));

        if (!getter && propertyMetadata.getSetter() == null) {
            propertyMetadata.setSetter(method);
            for (final Annotation annotation : method.getDeclaredAnnotations()) {
                propertyMetadata.addWriteAnnotation(annotation);
            }
        } else if (getter && propertyMetadata.getGetter() == null) {
            propertyMetadata.setGetter(method);
            for (final Annotation annotation : method.getDeclaredAnnotations()) {
                propertyMetadata.addReadAnnotation(annotation);
            }
        }

        return propertyName;
    }

    private static <T> PropertyMetadata<T> getOrCreateMethodPropertyMetadata(
            final String propertyName, final String declaringClassName,
            final Context context, final TypeData<T> typeData) {
        final PropertyMetadata<T> propertyMetadata = getOrCreatePropertyMetadata(propertyName, declaringClassName,
                context, typeData);
        if (!isAssignableClass(propertyMetadata.getTypeData().getType(), typeData.getType())) {
            propertyMetadata.setError(format("Property '%s' in %s, has differing data types: %s and %s.", propertyName,
                    declaringClassName, propertyMetadata.getTypeData(), typeData));
        }
        return propertyMetadata;
    }

    private static boolean isAssignableClass(final Class<?> propertyTypeClass, final Class<?> typeDataClass) {
        notNull("propertyTypeClass", propertyTypeClass);
        notNull("typeDataClass", typeDataClass);
        return propertyTypeClass.isAssignableFrom(typeDataClass) || typeDataClass.isAssignableFrom(propertyTypeClass);
    }

    @SuppressWarnings("unchecked")
    private static <T> PropertyMetadata<T> getOrCreatePropertyMetadata(
            final String propertyName, final String declaringClassName,
            final Context context, final TypeData<T> typeData) {
        return (PropertyMetadata<T>) context.propertyNameMap.computeIfAbsent(propertyName,
                k -> new PropertyMetadata<>(k, declaringClassName, typeData));
    }

    private static <T, S> void cachePropertyTypeData(final PropertyMetadata<T> propertyMetadata, final Context context,
                                                     final TypeData<S> parentClassTypeData,
                                                     final List<String> genericTypeNames, final Type genericType) {
        if (parentClassTypeData != null && !parentClassTypeData.getTypeParameters().isEmpty()) {
            final TypeParameterMap typeParameter = getTypeParameterMap(propertyMetadata, genericTypeNames, genericType);
            if (typeParameter.hasTypeParameters()) {
                context.propertyTypeParameterMap.put(propertyMetadata.getName(), typeParameter);
                propertyMetadata.typeParameterInfo(typeParameter, parentClassTypeData);
            }
        } else if (genericType instanceof TypeVariable) {
            throw new CodecConfigurationException(
                    String.format("Property '%s' in %s, has unsupported type variable: %s",
                            propertyMetadata.getName(), propertyMetadata.getDeclaringClassName(), genericType));
        }
    }

    private static Type getGenericType(final Method method) {
        return isGetter(method) ? method.getGenericReturnType() : method.getGenericParameterTypes()[0];
    }

    private static <T> TypeParameterMap getTypeParameterMap(final PropertyMetadata<T> propertyMetadata,
                                                            final List<String> genericTypeNames,
                                                            final Type propertyType) {
        final TypeParameterMap.Builder builder = TypeParameterMap.builder();
        buildTypeParameterMap(propertyMetadata, builder, genericTypeNames, propertyType, 0);
        return builder.build();
    }

    private static <T> int buildTypeParameterMap(final PropertyMetadata<T> propertyMetadata,
                                                 final TypeParameterMap.Builder builder,
                                                 final List<String> genericTypeNames,
                                                 final Type propertyType, final int start) {
        int index = start + 1;
        if (propertyType instanceof TypeVariable) {
            final TypeVariable<?> tv = (TypeVariable<?>) propertyType;
            if (tv.getGenericDeclaration() instanceof Class) {
                final int classParamIndex = genericTypeNames.indexOf(tv.getName());
                if (index == 1) {
                    builder.addIndex(classParamIndex);
                } else {
                    builder.addIndex(index - 1, classParamIndex);
                }
            } else {
                throw new CodecConfigurationException(
                        String.format("Property '%s' in %s, has unsupported generic method type: %s",
                                propertyMetadata.getName(), propertyMetadata.getDeclaringClassName(), propertyType));
            }
        } else if (propertyType instanceof ParameterizedType) {
            final ParameterizedType pt = (ParameterizedType) propertyType;
            for (Type actualType : pt.getActualTypeArguments()) {
                index = buildTypeParameterMap(propertyMetadata, builder, genericTypeNames, actualType, index);
            }
        }
        return index;
    }

    private static class Context {
        private final Set<String> propertyNames = new TreeSet<>();
        private final Map<String, PropertyMetadata<?>> propertyNameMap = new HashMap<>();
        private final ArrayList<Annotation> annotations = new ArrayList<>();
        private final Map<String, TypeParameterMap> propertyTypeParameterMap = new HashMap<>();

        private <T> void process(final ClassModelBuilder<T> classModelBuilder) {
            for (final String propertyName : propertyNames) {
                final PropertyMetadata<?> propertyMetadata = propertyNameMap.get(propertyName);
                classModelBuilder.removeProperty(propertyMetadata.getName());
                if (propertyMetadata.isSerializable() && propertyMetadata.isDeserializable()) {
                    classModelBuilder.addProperty(PojoBuilderHelper.createPropertyModelBuilder(propertyMetadata));
                } else {
                    throw new CodecConfigurationException(
                            String.format("Property '%s'[%s] in %s, need both getter[%s] and setter[%s]",
                                    propertyName, propertyMetadata.getTypeData(),
                                    propertyMetadata.getDeclaringClassName(),
                                    propertyMetadata.isSerializable(), propertyMetadata.isDeserializable()
                            )
                    );
                }
            }

            reverse(annotations);
            classModelBuilder.annotations(annotations);
            classModelBuilder.propertyNameToTypeParameterMap(propertyTypeParameterMap);
        }
    }
}
