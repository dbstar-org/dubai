package io.github.dbstarll.dubai.model.service;

import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.utils.PackageUtils;
import io.github.dbstarll.dubai.model.service.validation.GeneralValidation;
import io.github.dbstarll.dubai.model.service.validation.GeneralValidation.Position;
import io.github.dbstarll.dubai.model.service.validation.Validation;
import io.github.dbstarll.utils.lang.wrapper.EntryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

public final class ServiceFactory<E extends Entity, S extends Service<E>>
        implements InvocationHandler, ImplementalAutowirerAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceFactory.class);

    private final Class<S> serviceClass;
    private final io.github.dbstarll.dubai.model.collection.Collection<E> collection;
    private final Class<E> entityClass;
    private final ConcurrentMap<Class<?>, Implemental> implementals = new ConcurrentHashMap<>();
    private final Map<String, MethodValue> methods;
    private final java.util.Collection<PositionMethod> positionMethods;
    private final AtomicReference<java.util.Collection<PositionValidation<E>>> validationRef = new AtomicReference<>();

    private ImplementalAutowirer autowirer;

    private ServiceFactory(Class<S> serviceClass, io.github.dbstarll.dubai.model.collection.Collection<E> collection) {
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
            Class<S> serviceClass, Class<E> entityClass) {
        final Map<String, MethodValue> methods = new HashMap<>();
        final Map<String, PositionMethod> validationMethods = new HashMap<>();
        final java.util.Collection<PositionMethod> positionMethods = new LinkedList<>();

        for (Type type : getAllImplementationInterface(serviceClass)) {
            final Class<?> typeClass = getClass(type);
            for (Method m : typeClass.getMethods()) {
                methods.put(new MethodKey(m, entityClass).key, new MethodValue(typeClass, m));
            }

            for (Method m : typeClass.getAnnotation(Implementation.class).value().getMethods()) {
                final GeneralValidation validation = m.getAnnotation(GeneralValidation.class);
                if (validation != null && validation.value().isAssignableFrom(entityClass)
                        && Validation.class == m.getReturnType()) {
                    final MethodKey key = new MethodKey(m, entityClass);
                    final PositionMethod newValue = new PositionMethod(validation.position(), new MethodValue(typeClass, m));
                    final PositionMethod oldValue = validationMethods.get(key.key);
                    if (!newValue.equals(oldValue)) {
                        validationMethods.put(key.key, newValue);
                        if (oldValue != null) {
                            positionMethods.remove(oldValue);
                        }
                        positionMethods.add(newValue);
                    }
                }
            }
        }

        return EntryWrapper.wrap(methods, positionMethods);
    }

    private static List<Type> getAllImplementationInterface(Type serviceType) {
        final List<Type> list = new LinkedList<>();
        for (Type type : getClass(serviceType).getGenericInterfaces()) {
            list.addAll(getAllImplementationInterface(type));
            if (getClass(type).getAnnotation(Implementation.class) != null) {
                list.add(type);
            }
        }
        return list;
    }

    private static Class<?> getClass(Type type) {
        if (ParameterizedType.class.isInstance(type)) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        } else {
            return (Class<?>) type;
        }
    }

    @Override
    public void setImplementalAutowirer(ImplementalAutowirer implementalAutowirer) {
        this.autowirer = implementalAutowirer;
    }

    @SuppressWarnings("unchecked")
    private java.util.Collection<PositionValidation<E>> buildGeneralValidation(Object proxy) throws Throwable {
        if (validationRef.get() == null) {
            final java.util.Collection<PositionValidation<E>> validations = new LinkedList<>();
            for (PositionMethod entry : positionMethods) {
                final Implemental implemental = findOrPutImplemental(proxy, entry.getValue().key);
                if (implemental != null) {
                    final Validation<E> validation;
                    try {
                        validation = (Validation<E>) entry.getValue().value.invoke(implemental);
                    } catch (InvocationTargetException ex) {
                        throw ex.getTargetException();
                    }
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
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        } else if (method.getDeclaringClass() == ImplementalAutowirerAware.class) {
            return method.invoke(this, args);
        } else if (method.getDeclaringClass() == GeneralValidateable.class) {
            return buildGeneralValidation(proxy);
        }

        final MethodValue entry = methods.get(new MethodKey(method, entityClass).key);
        if (entry != null) {
            final Class<?> serviceInterface = entry.key;
            final Method overrideMethod = entry.value;
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

    private Implemental findOrPutImplemental(Object proxy, Class<?> serviceInterface) {
        if (!implementals.containsKey(serviceInterface)) {
            putImplemental(proxy, serviceInterface, getImplementalClass(serviceInterface));
        }
        return implementals.get(serviceInterface);
    }

    private <I extends Implemental> void putImplemental(Object proxy, final Class<?> serviceInterface,
                                                        final Class<I> implementalClass) {
        final Constructor<I> constructor = getConstructor(implementalClass, serviceClass);
        if (constructor != null) {
            try {
                final I implemental = constructor.newInstance(proxy, collection);
                if (implementals.putIfAbsent(serviceInterface, implemental) == null) {
                    LOGGER.debug("Implemental of service: {}[{}] use {}", serviceClass.getName(), serviceInterface.getName(),
                            implementalClass.getName());
                    if (autowirer != null) {
                        autowirer.autowire(implemental);
                    }
                    implemental.afterPropertiesSet();
                }
            } catch (Throwable ex) {
                implementals.remove(serviceInterface);
                LOGGER.error("不能实例化Implemental：" + implementalClass, ex);
            }
        }
    }

    private static Class<? extends Implemental> getImplementalClass(final Class<?> serviceInterface) {
        return serviceInterface.getAnnotation(Implementation.class).value();
    }

    private static <I extends Implemental> Constructor<I> getConstructor(final Class<I> implementalClass,
                                                                         final Class<?> serviceClass) {
        if (Modifier.isPublic(implementalClass.getModifiers()) && Modifier.isFinal(implementalClass.getModifiers())) {
            final Constructor<I> constructor = findConstructor(implementalClass, serviceClass);
            if (constructor != null) {
                LOGGER.info("Constructor of Implemental[{}] use {}", implementalClass, constructor);
                return constructor;
            } else {
                LOGGER.error("不能实例化Implemental，没有配套的构造函数：{}", implementalClass);
            }
        } else {
            LOGGER.error("Implemental is not public final: {}", implementalClass);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <I extends Implemental> Constructor<I> findConstructor(final Class<I> implementalClass,
                                                                          final Class<?> serviceClass) {
        for (Constructor<?> constructor : implementalClass.getConstructors()) {
            final Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length == 2 && io.github.dbstarll.dubai.model.collection.Collection.class == parameterTypes[1]
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
     * @return {@link Service}实例
     */
    @SuppressWarnings("unchecked")
    public static <E extends Entity, S extends Service<E>> S newInstance(Class<S> serviceClass,
                                                                         io.github.dbstarll.dubai.model.collection.Collection<E> collection) {
        if (isServiceClass(serviceClass)) {
            if (serviceClass.isInterface()) {
                final Class<?> packageInterface = PackageUtils.getPackageInterface(serviceClass, Package.class);
                return (S) Proxy.newProxyInstance(serviceClass.getClassLoader(),
                        new Class[]{serviceClass, ImplementalAutowirerAware.class, GeneralValidateable.class, packageInterface},
                        new ServiceFactory<>(serviceClass, collection));
            } else {
                try {
                    return serviceClass.newInstance();
                } catch (Throwable ex) {
                    throw new UnsupportedOperationException("Instantiation fails: " + serviceClass, ex);
                }
            }
        } else {
            throw new UnsupportedOperationException("Invalid ServiceClass: " + serviceClass);
        }
    }

    /**
     * 判断是否有效的实体类.
     *
     * @param serviceClass 实体类
     * @return 如果是一个有效的实体类，返回true，否则返回false
     */
    public static <E extends Entity, S extends Service<E>> boolean isServiceClass(Class<S> serviceClass) {
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
        return false;
    }

    /**
     * 获得代理对象的原始接口.
     *
     * @param proxy 代理对象
     * @return 代理对象的原始接口
     */
    @SuppressWarnings("unchecked")
    public static <E extends Entity, S extends Service<E>> Class<S> getServiceClass(S proxy) {
        if (Proxy.isProxyClass(proxy.getClass())) {
            final InvocationHandler handler = Proxy.getInvocationHandler(proxy);
            if (ServiceFactory.class.isInstance(handler)) {
                return ((ServiceFactory<E, S>) handler).serviceClass;
            }
        }
        return (Class<S>) proxy.getClass();
    }

    public interface GeneralValidateable<E extends Entity> {
        java.util.Collection<PositionValidation<E>> generalValidations();
    }

    public static class PositionValidation<E extends Entity> extends EntryWrapper<Position, Validation<E>> {
        public PositionValidation(Position key, Validation<E> value) {
            super(key, value);
        }
    }

    private static class PositionMethod extends EntryWrapper<Position, MethodValue> {
        public PositionMethod(Position key, MethodValue value) {
            super(key, value);
        }
    }
}
