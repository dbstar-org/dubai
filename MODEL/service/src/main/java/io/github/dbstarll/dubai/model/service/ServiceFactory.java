package io.github.dbstarll.dubai.model.service;

import io.github.dbstarll.dubai.model.collection.BaseCollection;
import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.collection.CollectionFactory;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.entity.utils.PackageUtils;
import io.github.dbstarll.dubai.model.service.validation.GeneralValidation;
import io.github.dbstarll.dubai.model.service.validation.GeneralValidation.Position;
import io.github.dbstarll.dubai.model.service.validation.Validation;
import io.github.dbstarll.utils.lang.wrapper.EntryWrapper;
import org.bson.BsonReader;
import org.bson.codecs.DecoderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import static org.apache.commons.lang3.Validate.notNull;

public final class ServiceFactory<E extends Entity, S extends Service<E>>
        implements InvocationHandler, ImplementalAutowirerAware, ServiceHelper<E> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceFactory.class);

    private final Class<S> serviceClass;
    private final Collection<E> collection;
    private final Class<E> entityClass;
    private final ConcurrentMap<Class<?>, Implemental> implementals = new ConcurrentHashMap<>();
    private final Map<String, MethodValue> methods;
    private final java.util.Collection<PositionMethod> positionMethods;
    private final AtomicReference<java.util.Collection<PositionValidation<E>>> validationRef = new AtomicReference<>();

    private ImplementalAutowirer autowirer;

    private ServiceFactory(final Class<S> serviceClass, final Collection<E> collection) {
        this.serviceClass = serviceClass;
        this.collection = collection;
        this.entityClass = collection.getEntityClass();
        final Entry<Map<String, MethodValue>, java.util.Collection<PositionMethod>> ms = getMethods(serviceClass,
                entityClass);
        this.methods = ms.getKey();
        this.positionMethods = ms.getValue();
    }

    private static <E extends Entity,
            S extends Service<E>> Entry<Map<String, MethodValue>, java.util.Collection<PositionMethod>> getMethods(
            final Class<S> serviceClass, final Class<E> entityClass) {
        final Map<String, MethodValue> methods = new HashMap<>();
        final Map<String, PositionMethod> validations = new HashMap<>();
        final java.util.Collection<PositionMethod> positionMethods = new LinkedList<>();

        for (Type type : getAllImplementationInterface(serviceClass)) {
            final Class<?> typeClass = getClass(type);
            for (Method m : typeClass.getMethods()) {
                methods.put(new MethodKey(m, entityClass).getKey(), new MethodValue(typeClass, m));
            }
            for (Method m : notNull(getImplementalClass(typeClass)).getMethods()) {
                collectPositionMethods(findPositionMethod(m, typeClass, entityClass), validations, positionMethods);
            }
        }

        return EntryWrapper.wrap(methods, positionMethods);
    }

    private static void collectPositionMethods(final Entry<String, PositionMethod> entry,
                                               final Map<String, PositionMethod> validationMethods,
                                               final java.util.Collection<PositionMethod> positionMethods) {
        if (entry != null) {
            final PositionMethod newValue = entry.getValue();
            final PositionMethod oldValue = validationMethods.get(entry.getKey());
            if (!newValue.equals(oldValue)) {
                validationMethods.put(entry.getKey(), newValue);
                if (oldValue != null) {
                    positionMethods.remove(oldValue);
                }
                positionMethods.add(newValue);
            }
        }
    }

    private static <E extends Entity> Entry<String, PositionMethod> findPositionMethod(
            final Method m, final Class<?> typeClass, final Class<E> entityClass) {
        if (Validation.class == m.getReturnType()) {
            final GeneralValidation validation = m.getAnnotation(GeneralValidation.class);
            if (isValidGeneralValidation(validation, entityClass)) {
                final MethodKey key = new MethodKey(m, entityClass);
                final MethodValue value = new MethodValue(typeClass, m);
                final PositionMethod newValue = new PositionMethod(validation.position(), value);
                return EntryWrapper.wrap(key.getKey(), newValue);
            }
        }
        return null;
    }

    private static <E extends Entity> boolean isValidGeneralValidation(final GeneralValidation validation,
                                                                       final Class<E> entityClass) {
        return validation != null && validation.value().isAssignableFrom(entityClass);
    }

    private static List<Type> getAllImplementationInterface(final Type serviceType) {
        final List<Type> list = new LinkedList<>();
        for (Type type : getClass(serviceType).getGenericInterfaces()) {
            list.addAll(getAllImplementationInterface(type));
            if (getImplementalClass(getClass(type)) != null) {
                list.add(type);
            }
        }
        return list;
    }

    private static Class<?> getClass(final Type type) {
        if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        } else {
            return (Class<?>) type;
        }
    }

    @Override
    public void setImplementalAutowirer(final ImplementalAutowirer implementalAutowirer) {
        this.autowirer = implementalAutowirer;
    }

    @Override
    public BaseCollection<E> getBaseCollection() {
        return CollectionFactory.getBaseCollection(collection);
    }

    @Override
    public E decode(final BsonReader reader, final DecoderContext decoderContext) {
        return getMongoCollection().getCodecRegistry().get(entityClass).decode(reader, decoderContext);
    }

    @SuppressWarnings("unchecked")
    private java.util.Collection<PositionValidation<E>> buildGeneralValidation(final Object proxy)
            throws InvocationTargetException, IllegalAccessException {
        if (validationRef.get() == null) {
            final java.util.Collection<PositionValidation<E>> validations = new LinkedList<>();
            for (PositionMethod entry : positionMethods) {
                final Implemental implemental = findOrPutImplemental(proxy, entry.getValue().getKey());
                if (implemental != null) {
                    final Validation<E> validation = (Validation<E>) entry.getValue().getValue().invoke(implemental);
                    if (validation != null) {
                        validations.add(new PositionValidation<>(entry.getKey(), validation));
                    }
                }
            }
            validationRef.compareAndSet(null, Collections.unmodifiableCollection(validations));
        }
        return validationRef.get();
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        } else if (method.getDeclaringClass() == ImplementalAutowirerAware.class) {
            return method.invoke(this, args);
        } else if (method.getDeclaringClass() == ServiceHelper.class) {
            return method.invoke(this, args);
        } else if (method.getDeclaringClass() == GeneralValidateable.class) {
            try {
                return buildGeneralValidation(proxy);
            } catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            }
        }

        final MethodValue entry = methods.get(new MethodKey(method, entityClass).getKey());
        if (entry != null) {
            final Class<?> serviceInterface = entry.getKey();
            final Method overrideMethod = entry.getValue();
            if (!overrideMethod.equals(method)) {
                LOGGER.debug("Override Method [{}] with [{}]", method, serviceInterface.getName());
            }
            final Implemental implemental = findOrPutImplemental(proxy, serviceInterface);
            if (implemental != null) {
                try {
                    return overrideMethod.invoke(implemental, args);
                } catch (InvocationTargetException ex) {
                    throw ex.getTargetException();
                }
            }
        }

        throw new UnsupportedOperationException(method.toString());
    }

    private Implemental findOrPutImplemental(final Object proxy, final Class<?> serviceInterface) {
        return implementals.computeIfAbsent(serviceInterface, i -> newImplemental(proxy, i, getImplementalClass(i)));
    }

    private <I extends Implemental> I newImplemental(final Object proxy, final Class<?> serviceInterface,
                                                     final Class<I> implementalClass) {
        final Constructor<I> constructor = getConstructor(implementalClass, serviceClass);
        if (constructor != null) {
            final I implemental;
            try {
                implemental = constructor.newInstance(proxy, collection);
            } catch (Exception ex) {
                throw new ImplementalInstanceException(implementalClass, ex);
            }
            LOGGER.debug("Implemental of service: {}[{}] use {}",
                    serviceClass.getName(), serviceInterface.getName(), implementalClass.getName());
            if (autowirer != null) {
                autowirer.autowire(implemental);
            }
            implemental.afterPropertiesSet();
            return implemental;
        }
        return null;
    }

    private static Class<? extends Implemental> getImplementalClass(final Class<?> serviceInterface) {
        final Implementation implementation = serviceInterface.getAnnotation(Implementation.class);
        if (implementation != null) {
            final Class<? extends Implemental> implementalClass = implementation.value();
            final int modifiers = implementalClass.getModifiers();
            if (Modifier.isPublic(modifiers) && Modifier.isFinal(modifiers)) {
                return implementalClass;
            } else {
                LOGGER.error("Implemental is not public final: {}", implementalClass);
            }
        }
        return null;
    }

    private static <I extends Implemental> Constructor<I> getConstructor(final Class<I> implementalClass,
                                                                         final Class<?> serviceClass) {
        final Constructor<I> constructor = findConstructor(implementalClass, serviceClass);
        if (constructor != null) {
            LOGGER.info("Constructor of Implemental[{}] use {}", implementalClass, constructor);
            return constructor;
        } else {
            LOGGER.error("不能实例化Implemental，没有配套的构造函数：{}", implementalClass);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static <I extends Implemental> Constructor<I> findConstructor(final Class<I> implementalClass,
                                                                          final Class<?> serviceClass) {
        for (Constructor<?> constructor : implementalClass.getConstructors()) {
            final Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length == 2 && Collection.class == parameterTypes[1]
                    && parameterTypes[0].isAssignableFrom(serviceClass)) {
                return (Constructor<I>) constructor;
            }
        }
        return null;
    }

    /**
     * 构造{@link Service}实例.
     *
     * @param serviceClass {@link Service}类
     * @param collection   {@link io.github.dbstarll.dubai.model.collection.Collection}实例
     * @param <E>          实体类
     * @param <S>          服务类
     * @return {@link Service}实例
     */
    @SuppressWarnings("unchecked")
    public static <E extends Entity, S extends Service<E>> S newInstance(final Class<S> serviceClass,
                                                                         final Collection<E> collection) {
        if (isServiceClass(serviceClass)) {
            if (serviceClass.isInterface()) {
                final Class<?> packageInterface = PackageUtils.getPackageInterface(serviceClass, Package.class);
                return (S) Proxy.newProxyInstance(serviceClass.getClassLoader(),
                        new Class[]{serviceClass, ServiceProxy.class, ImplementalAutowirerAware.class,
                                ServiceHelper.class, GeneralValidateable.class, packageInterface},
                        new ServiceFactory<>(serviceClass, collection));
            } else {
                try {
                    return serviceClass.getDeclaredConstructor().newInstance();
                } catch (Exception ex) {
                    throw new UnsupportedOperationException("Instantiation fails: " + serviceClass, ex);
                }
            }
        } else {
            throw new UnsupportedOperationException("Invalid ServiceClass: " + serviceClass);
        }
    }

    /**
     * 判断是否有效的服务类.
     *
     * @param serviceClass 服务类
     * @return 如果是一个有效的服务类，返回true，否则返回false
     */
    public static boolean isServiceClass(final Class<?> serviceClass) {
        if (Service.class.isAssignableFrom(serviceClass)) {
            if (!Modifier.isAbstract(serviceClass.getModifiers())) {
                if (serviceClass.getAnnotation(EntityService.class) != null) {
                    try {
                        serviceClass.getConstructor();
                    } catch (NoSuchMethodException | SecurityException e) {
                        return false;
                    }
                    return true;
                }
            } else if (serviceClass.isInterface()) {
                return serviceClass.getAnnotation(EntityService.class) != null;
            }
        }
        return false;
    }

    /**
     * 判断是否有效的接口型服务类.
     *
     * @param serviceClass 服务类
     * @return 如果是一个有效的接口型服务类，返回true，否则返回false
     */
    public static boolean isServiceInterface(final Class<?> serviceClass) {
        return isServiceClass(serviceClass) && serviceClass.isInterface();
    }

    /**
     * 判断是否有效的代理型服务类.
     *
     * @param proxyClass 代理类
     * @return 如果是一个有效的代理型服务类，返回true，否则返回false
     */
    public static boolean isServiceProxy(final Class<?> proxyClass) {
        return Proxy.isProxyClass(proxyClass) && ServiceProxy.class.isAssignableFrom(proxyClass)
                && isServiceInterface(getServiceClass(proxyClass));
    }

    /**
     * 获得代理对象的原始接口.
     *
     * @param proxy 代理对象
     * @param <E>   实体类
     * @param <S>   服务类
     * @return 代理对象的原始接口
     */
    @SuppressWarnings("unchecked")
    public static <E extends Entity, S extends Service<E>> Class<S> getServiceClass(final S proxy) {
        if (isServiceProxy(proxy.getClass())) {
            final InvocationHandler handler = Proxy.getInvocationHandler(proxy);
            if (handler instanceof ServiceFactory) {
                return ((ServiceFactory<E, S>) handler).serviceClass;
            }
        }
        return (Class<S>) proxy.getClass();
    }


    /**
     * 获得代理类的原始接口.
     *
     * @param proxyClass 代理类
     * @param <S>        服务类
     * @return 代理类的原始接口
     */
    @SuppressWarnings("unchecked")
    public static <S> Class<S> getServiceClass(final Class<S> proxyClass) {
        Class<S> c = proxyClass;
        if (Proxy.isProxyClass(proxyClass)) {
            for (Class<?> i : proxyClass.getInterfaces()) {
                if (isServiceInterface(i)) {
                    c = (Class<S>) i;
                }
            }
        }
        return c;
    }

    /**
     * 获得一个服务类对应的实体类.
     *
     * @param serviceClass 服务类
     * @param <E>          实体类
     * @param <S>          服务类
     * @return 实体类
     */
    public static <E extends Entity, S extends Service<E>> Class<E> getEntityClass(final Class<S> serviceClass) {
        final Type genericSuperclass = serviceClass.getGenericSuperclass();
        if (genericSuperclass != null) {
            final Class<E> entityClass = getEntityClassFromGeneric(genericSuperclass);
            if (entityClass != null) {
                return entityClass;
            }
        }
        for (Type genericInterface : serviceClass.getGenericInterfaces()) {
            final Class<E> entityClass = getEntityClassFromGeneric(genericInterface);
            if (entityClass != null) {
                return entityClass;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <E extends Entity> Class<E> getEntityClassFromGeneric(final Type genericType) {
        if (genericType instanceof ParameterizedType) {
            for (Type type : ((ParameterizedType) genericType).getActualTypeArguments()) {
                if (type instanceof Class && EntityFactory.isEntityClass((Class<?>) type)) {
                    return (Class<E>) type;
                }
            }
        }
        return null;
    }

    public interface GeneralValidateable<E extends Entity> {
        /**
         * 获得常规校验集合.
         *
         * @return 常规校验集合
         */
        java.util.Collection<PositionValidation<E>> generalValidations();
    }

    public static class PositionValidation<E extends Entity> extends EntryWrapper<Position, Validation<E>> {
        /**
         * 构造一个带位置属性的校验.
         *
         * @param key   位置
         * @param value 校验
         */
        public PositionValidation(final Position key, final Validation<E> value) {
            super(key, value);
        }
    }

    private static class PositionMethod extends EntryWrapper<Position, MethodValue> {
        PositionMethod(final Position key, final MethodValue value) {
            super(key, value);
        }
    }

    public interface ServiceProxy {
    }
}
