package org.bson.codecs.pojo;

import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.EntityModifier;
import io.github.dbstarll.dubai.model.mongodb.EntityInstanceCreatorFactory;
import org.bson.codecs.configuration.CodecConfigurationException;
import org.bson.codecs.pojo.PropertyReflectionUtils.PropertyMethods;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public final class EntityConvention implements Convention {
    @Override
    public void apply(ClassModelBuilder<?> classModelBuilder) {
        process(classModelBuilder);
    }

    @SuppressWarnings("unchecked")
    private <T> void process(final ClassModelBuilder<T> classModelBuilder) {
        final Class<T> entityClass = classModelBuilder.getType();
        if (Entity.class.isAssignableFrom(entityClass) && entityClass.isInterface()) {
            processEntity((ClassModelBuilder<? extends Entity>) classModelBuilder);
        }
    }

    private <T extends Entity> void processEntity(final ClassModelBuilder<T> classModelBuilder) {
        final Class<T> entityClass = classModelBuilder.getType();

        final Set<String> propertyNames = new TreeSet<>();
        final Map<String, PropertyMetadata<?>> propertyNameMap = new HashMap<>();
        final ArrayList<Annotation> annotations = new ArrayList<>();
        final Map<String, TypeParameterMap> propertyTypeParameterMap = new HashMap<>();

        collectMethods(EntityModifier.class, propertyNames, propertyNameMap, annotations, propertyTypeParameterMap, null);
        collectMethods(entityClass, propertyNames, propertyNameMap, annotations, propertyTypeParameterMap, null);

        for (final String propertyName : propertyNames) {
            final PropertyMetadata<?> propertyMetadata = propertyNameMap.get(propertyName);
            if (propertyMetadata.isSerializable() && propertyMetadata.isDeserializable()) {
                classModelBuilder.addProperty(createPropertyModelBuilder(propertyMetadata));
            } else {
                throw new CodecConfigurationException(
                        String.format("Property '%s' in %s with data types: %s, need both getter[%s] and setter[%s]", propertyName,
                                propertyMetadata.getDeclaringClassName(), propertyMetadata.getTypeData(),
                                propertyMetadata.isSerializable(), propertyMetadata.isDeserializable()));
            }
        }

        classModelBuilder.annotations(annotations);
        classModelBuilder.propertyNameToTypeParameterMap(propertyTypeParameterMap);

        classModelBuilder.instanceCreatorFactory(new EntityInstanceCreatorFactory<>(entityClass));
    }

    private static <T, S> void collectMethods(final Class<T> entityInterface, final Set<String> propertyNames,
                                              final Map<String, PropertyMetadata<?>> propertyNameMap, final ArrayList<Annotation> annotations,
                                              final Map<String, TypeParameterMap> propertyTypeParameterMap, final TypeData<S> parentClassTypeData) {
        for (Type t : entityInterface.getGenericInterfaces()) {
            final TypeData<T> classTypeData = TypeData.newInstance(t, entityInterface);
            if (t instanceof ParameterizedType) {
                t = ((ParameterizedType) t).getRawType();
            }
            collectMethods((Class<?>) t, propertyNames, propertyNameMap, annotations, propertyTypeParameterMap,
                    classTypeData);
        }

        final String declaringClassName = entityInterface.toString();
        final List<String> genericTypeNames = new ArrayList<>();
        for (TypeVariable<? extends Class<? super T>> classTypeVariable : entityInterface.getTypeParameters()) {
            genericTypeNames.add(classTypeVariable.getName());
        }

        final PropertyMethods propertyMethods = PropertyReflectionUtils.getPropertyMethods(entityInterface);
        for (Method method : propertyMethods.getSetterMethods()) {
            propertyNames.add(collectMethod(declaringClassName, propertyNameMap, propertyTypeParameterMap,
                    parentClassTypeData, genericTypeNames, method, false));
        }
        for (Method method : propertyMethods.getGetterMethods()) {
            propertyNames.add(collectMethod(declaringClassName, propertyNameMap, propertyTypeParameterMap,
                    parentClassTypeData, genericTypeNames, method, true));
        }

        annotations.addAll(Arrays.asList(entityInterface.getDeclaredAnnotations()));
    }

    private static <S> String collectMethod(final String declaringClassName,
                                            final Map<String, PropertyMetadata<?>> propertyNameMap,
                                            final Map<String, TypeParameterMap> propertyTypeParameterMap, final TypeData<S> parentClassTypeData,
                                            final List<String> genericTypeNames, final Method method, final boolean getter) {
        final String propertyName = PropertyReflectionUtils.toPropertyName(method);

        final Type genericType = getter ? method.getGenericReturnType() : method.getGenericParameterTypes()[0];
        final PropertyMetadata<?> propertyMetadata = getOrCreateMethodPropertyMetadata(propertyName, declaringClassName,
                propertyNameMap, TypeData.newInstance(method), propertyTypeParameterMap, parentClassTypeData, genericTypeNames,
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

    private static <T, S> PropertyMetadata<T> getOrCreateMethodPropertyMetadata(final String propertyName,
                                                                                final String declaringClassName, final Map<String, PropertyMetadata<?>> propertyNameMap,
                                                                                final TypeData<T> typeData, final Map<String, TypeParameterMap> propertyTypeParameterMap,
                                                                                final TypeData<S> parentClassTypeData, final List<String> genericTypeNames, final Type genericType) {
        final PropertyMetadata<T> propertyMetadata = getOrCreatePropertyMetadata(propertyName, declaringClassName,
                propertyNameMap, typeData);
        if (!propertyMetadata.getTypeData().getType().isAssignableFrom(typeData.getType())) {
            throw new CodecConfigurationException(String.format("Property '%s' in %s, has differing data types: %s and %s",
                    propertyName, declaringClassName, propertyMetadata.getTypeData(), typeData));
        }
        cachePropertyTypeData(propertyMetadata, propertyTypeParameterMap, parentClassTypeData, genericTypeNames,
                genericType);
        return propertyMetadata;
    }

    @SuppressWarnings("unchecked")
    private static <T> PropertyMetadata<T> getOrCreatePropertyMetadata(final String propertyName,
                                                                       final String declaringClassName, final Map<String, PropertyMetadata<?>> propertyNameMap,
                                                                       final TypeData<T> typeData) {
        return (PropertyMetadata<T>) propertyNameMap.computeIfAbsent(propertyName, k -> new PropertyMetadata<>(k, declaringClassName, typeData));
    }

    private static <T, S> void cachePropertyTypeData(final PropertyMetadata<T> propertyMetadata,
                                                     final Map<String, TypeParameterMap> propertyTypeParameterMap, final TypeData<S> parentClassTypeData,
                                                     final List<String> genericTypeNames, final Type genericType) {
        if (parentClassTypeData != null && !parentClassTypeData.getTypeParameters().isEmpty()) {
            final TypeParameterMap typeParameterMap = getTypeParameterMap(propertyMetadata, genericTypeNames, genericType);
            if (typeParameterMap.hasTypeParameters()) {
                propertyTypeParameterMap.put(propertyMetadata.getName(), typeParameterMap);
                propertyMetadata.typeParameterInfo(typeParameterMap, parentClassTypeData);
            }
        } else if (genericType instanceof TypeVariable) {
            throw new CodecConfigurationException(String.format("Property '%s' in %s, has unsupported type variable: %s",
                    propertyMetadata.getName(), propertyMetadata.getDeclaringClassName(), genericType));
        }
    }

    private static <T> TypeParameterMap getTypeParameterMap(final PropertyMetadata<T> propertyMetadata,
                                                            final List<String> genericTypeNames, final Type propertyType) {
        final TypeParameterMap.Builder builder = TypeParameterMap.builder();
        buildTypeParameterMap(propertyMetadata, builder, genericTypeNames, propertyType, 0);
        return builder.build();
    }

    private static <T> int buildTypeParameterMap(final PropertyMetadata<T> propertyMetadata,
                                                 final TypeParameterMap.Builder builder, final List<String> genericTypeNames, final Type propertyType, int index) {
        index++;
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
                        String.format("Property '%s' in %s, has unsupported generic method type: %s", propertyMetadata.getName(),
                                propertyMetadata.getDeclaringClassName(), propertyType));
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
                .readAnnotations(propertyMetadata.getReadAnnotations()).writeAnnotations(propertyMetadata.getWriteAnnotations())
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
                                                           final TypeParameterMap typeParameterMap, final List<TypeData<?>> parentClassTypeParameters) {
        final TypeData<V> specializedFieldType;
        final Map<Integer, Either<Integer, TypeParameterMap>> fieldToClassParamIndexMap = typeParameterMap.getPropertyToClassParamIndexMap();
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
    private static <V> TypeData<V> specializeFieldType(final Map<Integer, Either<Integer, TypeParameterMap>> fieldToClassParamIndexMap,
                                                       final List<TypeData<?>> parentClassTypeParameters, final TypeData<V> typeData, AtomicInteger index) {
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
}