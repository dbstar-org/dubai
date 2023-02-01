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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        for (Type t : entityInterface.getGenericInterfaces()) {
            final TypeData<T> classTypeData = TypeData.newInstance(t, entityInterface);
            if (t instanceof ParameterizedType) {
                t = ((ParameterizedType) t).getRawType();
            }
            collectMethods((Class<?>) t, context, classTypeData);
        }

        final String declaringClassName = entityInterface.toString();
        final List<String> genericTypeNames = Arrays.stream(entityInterface.getTypeParameters())
                .map(TypeVariable::getName).collect(Collectors.toList());

        final PropertyMethods propertyMethods = PropertyReflectionUtils.getPropertyMethods(entityInterface);
        propertyMethods.getSetterMethods().stream().map(method -> collectMethod(declaringClassName, context,
                parentClassTypeData, genericTypeNames, method, false)).forEach(context.propertyNames::add);
        propertyMethods.getGetterMethods().stream().map(method -> collectMethod(declaringClassName, context,
                parentClassTypeData, genericTypeNames, method, true)).forEach(context.propertyNames::add);

        context.annotations.addAll(Arrays.asList(entityInterface.getDeclaredAnnotations()));
    }

    private static <S> String collectMethod(final String declaringClassName, final Context context,
                                            final TypeData<S> parentClassTypeData, final List<String> genericTypeNames,
                                            final Method method, final boolean getter) {
        final String propertyName = PropertyReflectionUtils.toPropertyName(method);

        final Type genericType = getter ? method.getGenericReturnType() : method.getGenericParameterTypes()[0];
        final PropertyMetadata<?> propertyMetadata = getOrCreateMethodPropertyMetadata(
                propertyName, declaringClassName, context, TypeData.newInstance(method));
        cachePropertyTypeData(propertyMetadata, context, parentClassTypeData, genericTypeNames,
                genericType);
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
        if (!propertyMetadata.getTypeData().getType().isAssignableFrom(typeData.getType())) {
            throw new CodecConfigurationException(
                    String.format("Property '%s' in %s, has differing data types: %s and %s",
                            propertyName, declaringClassName, propertyMetadata.getTypeData(), typeData));
        }
        return propertyMetadata;
    }

    @SuppressWarnings("unchecked")
    private static <T> PropertyMetadata<T> getOrCreatePropertyMetadata(
            final String propertyName, final String declaringClassName,
            final Context context, final TypeData<T> typeData) {
        return (PropertyMetadata<T>) context.propertyMetadatas.computeIfAbsent(propertyName,
                k -> new PropertyMetadata<>(k, declaringClassName, typeData));
    }

    private static <T, S> void cachePropertyTypeData(final PropertyMetadata<T> propertyMetadata, final Context context,
                                                     final TypeData<S> parentClassTypeData,
                                                     final List<String> genericTypeNames, final Type genericType) {
        if (parentClassTypeData != null && !parentClassTypeData.getTypeParameters().isEmpty()) {
            final TypeParameterMap typeParameter = getTypeParameterMap(propertyMetadata, genericTypeNames, genericType);
            if (typeParameter.hasTypeParameters()) {
                context.typeParameters.put(propertyMetadata.getName(), typeParameter);
                propertyMetadata.typeParameterInfo(typeParameter, parentClassTypeData);
            }
        } else if (genericType instanceof TypeVariable) {
            throw new CodecConfigurationException(
                    String.format("Property '%s' in %s, has unsupported type variable: %s",
                            propertyMetadata.getName(), propertyMetadata.getDeclaringClassName(), genericType));
        }
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

    private static <T> PropertyModelBuilder<T> createPropertyModelBuilder(final PropertyMetadata<T> propertyMetadata) {
        final PropertyModelBuilder<T> propertyModelBuilder = PropertyModel.builder();
        propertyModelBuilder.propertyName(propertyMetadata.getName()).readName(propertyMetadata.getName())
                .writeName(propertyMetadata.getName()).typeData(propertyMetadata.getTypeData())
                .readAnnotations(propertyMetadata.getReadAnnotations())
                .writeAnnotations(propertyMetadata.getWriteAnnotations())
                .propertySerialization(new PropertyModelSerializationImpl<>())
                .propertyAccessor(new PropertyAccessorImpl<>(propertyMetadata));

        if (propertyMetadata.getTypeParameters() != null) {
            //TODO 检查这一段代码的逻辑
            specializePropertyModelBuilder(propertyModelBuilder, propertyMetadata.getTypeParameterMap(),
                    propertyMetadata.getTypeParameters());
        }

        return propertyModelBuilder;
    }

    @SuppressWarnings("unchecked")
    private static <V> void specializePropertyModelBuilder(final PropertyModelBuilder<V> propertyModelBuilder,
                                                           final TypeParameterMap typeParameterMap,
                                                           final List<TypeData<?>> parentClassTypeParameters) {
        final TypeData<V> specializedFieldType;
        final Map<Integer, Either<Integer, TypeParameterMap>> fieldToClassParamIndexMap
                = typeParameterMap.getPropertyToClassParamIndexMap();
        final Either<Integer, TypeParameterMap> classTypeParamRepresentsWholeField = fieldToClassParamIndexMap.get(-1);
        if (classTypeParamRepresentsWholeField != null) {
            final Integer index = classTypeParamRepresentsWholeField.map(Function.identity(), r -> null);
            if (index != null) {
                specializedFieldType = (TypeData<V>) parentClassTypeParameters.get(index);
            } else {
                specializedFieldType = specializeFieldType(fieldToClassParamIndexMap, parentClassTypeParameters,
                        propertyModelBuilder.getTypeData(), new AtomicInteger());
            }
        } else {
            specializedFieldType = specializeFieldType(fieldToClassParamIndexMap, parentClassTypeParameters,
                    propertyModelBuilder.getTypeData(), new AtomicInteger());
        }
        propertyModelBuilder.typeData(specializedFieldType);
    }

    @SuppressWarnings("unchecked")
    private static <V> TypeData<V> specializeFieldType(
            final Map<Integer, Either<Integer, TypeParameterMap>> fieldToClassParamIndexMap,
            final List<TypeData<?>> parentClassTypeParameters, final TypeData<V> typeData, final AtomicInteger index) {
        final Either<Integer, TypeParameterMap> paramIndex = fieldToClassParamIndexMap.get(index.get());
        index.incrementAndGet();
        if (paramIndex != null) {
            final Integer realIndex = paramIndex.map(Function.identity(), r -> null);
            if (realIndex != null) {
                return (TypeData<V>) parentClassTypeParameters.get(realIndex);
            }
        }

        final TypeData.Builder<V> builder = TypeData.builder(typeData.getType());
        for (TypeData<?> typeParameter : typeData.getTypeParameters()) {
            builder.addTypeParameter(
                    specializeFieldType(fieldToClassParamIndexMap, parentClassTypeParameters, typeParameter, index));
        }
        return builder.build();
    }

    private static class Context {
        private final Set<String> propertyNames = new TreeSet<>();
        private final Map<String, PropertyMetadata<?>> propertyMetadatas = new HashMap<>();
        private final ArrayList<Annotation> annotations = new ArrayList<>();
        private final Map<String, TypeParameterMap> typeParameters = new HashMap<>();

        private <T> void process(final ClassModelBuilder<T> classModelBuilder) {
            for (final String propertyName : propertyNames) {
                final PropertyMetadata<?> propertyMetadata = propertyMetadatas.get(propertyName);
                if (propertyMetadata.isSerializable() && propertyMetadata.isDeserializable()) {
                    classModelBuilder.removeProperty(propertyMetadata.getName());
                    classModelBuilder.addProperty(createPropertyModelBuilder(propertyMetadata));
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
            classModelBuilder.annotations(annotations);
            classModelBuilder.propertyNameToTypeParameterMap(typeParameters);
        }
    }
}
