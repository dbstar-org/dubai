package io.github.dbstarll.dubai.model.entity;

import io.github.dbstarll.dubai.model.entity.utils.PackageUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.CloneFailedException;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class EntityFactory<E extends Entity> implements InvocationHandler, Serializable {
    private static final long serialVersionUID = 1190830425462840117L;

    private static final String PREFIX_READ = "get";
    private static final String PREFIX_READ_BOOL = "is";
    private static final String PREFIX_WRITE = "set";
    private static final int LEN_PREFIX_READ = PREFIX_READ.length();
    private static final int LEN_PREFIX_READ_BOOL = PREFIX_READ_BOOL.length();
    private static final int LEN_PREFIX_WRITE = PREFIX_WRITE.length();

    private static final Map<Class<?>, Serializable> DEFAULT_VALUES = getDefaultValues();
    private static final ConcurrentMap<Class<?>, Map<String, Serializable>> DEFAULT_FIELDS = new ConcurrentHashMap<>();

    private final Class<E> entityClass;
    private final ConcurrentMap<String, Serializable> fields;

    private EntityFactory(final Class<E> entityClass, final Map<String, Serializable> fields) {
        this.entityClass = entityClass;
        this.fields = fields == null ? new ConcurrentHashMap<>() : new ConcurrentHashMap<>(fields);
        setDefaultValue(this.fields, getDefaultPrimitiveFields(entityClass));
    }

    private static Map<Class<?>, Serializable> getDefaultValues() {
        final Map<Class<?>, Serializable> values = new HashMap<>();
        values.put(Byte.TYPE, (byte) 0);
        values.put(Short.TYPE, (short) 0);
        values.put(Integer.TYPE, 0);
        values.put(Long.TYPE, 0L);
        values.put(Boolean.TYPE, false);
        values.put(Character.TYPE, '\u0000');
        values.put(Float.TYPE, 0.0f);
        values.put(Double.TYPE, 0.0d);
        return values;
    }

    private static Map<String, Serializable> getDefaultPrimitiveFields(final Class<?> entityClass) {
        if (!DEFAULT_FIELDS.containsKey(entityClass)) {
            final Map<String, Serializable> fileds = new HashMap<>();
            for (Method method : entityClass.getMethods()) {
                final String fieldName = getWriteProperty(method);
                if (null != fieldName) {
                    final Class<?> fieldType = method.getParameterTypes()[0];
                    if (fieldType.isPrimitive()) {
                        fileds.put(fieldName, DEFAULT_VALUES.get(fieldType));
                    }
                }
            }
            DEFAULT_FIELDS.putIfAbsent(entityClass, fileds);
        }
        return DEFAULT_FIELDS.get(entityClass);
    }

    private static String getWriteProperty(final Method method) {
        if (method.getReturnType() == Void.TYPE && method.getParameterTypes().length == 1
                && method.getName().startsWith(PREFIX_WRITE)) {
            return getReplaceProperty(StringUtils.uncapitalize(method.getName().substring(LEN_PREFIX_WRITE)));
        } else {
            return null;
        }
    }

    private static String getReadProperty(final Method method) {
        if (method.getReturnType() != Void.TYPE) {
            if (method.getName().startsWith(PREFIX_READ)) {
                return getReplaceProperty(StringUtils.uncapitalize(method.getName().substring(LEN_PREFIX_READ)));
            } else if (method.getName().startsWith(PREFIX_READ_BOOL)) {
                return getReplaceProperty(StringUtils.uncapitalize(method.getName().substring(LEN_PREFIX_READ_BOOL)));
            }
        }
        return null;
    }

    private static String getReplaceProperty(final String property) {
        return "id".equals(property) ? Entity.FIELD_NAME_ID : property;
    }

    private static void setDefaultValue(final ConcurrentMap<String, Serializable> fields,
                                        final Map<String, Serializable> values) {
        for (Entry<String, Serializable> entry : values.entrySet()) {
            fields.putIfAbsent(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        } else if (method.getDeclaringClass() == PojoFields.class) {
            return fields;
        }

        final int argsLangth = ArrayUtils.getLength(args);
        if (argsLangth == 1) {
            final String property = getWriteProperty(method);
            if (StringUtils.isNotBlank(property)) {
                if (null == args[0]) {
                    fields.remove(property);
                    return null;
                } else if (args[0] instanceof Serializable) {
                    fields.put(property, (Serializable) args[0]);
                    return null;
                }
            }
        } else if (argsLangth == 0) {
            final String property = getReadProperty(method);
            if (StringUtils.isNotBlank(property)) {
                return fields.get(property);
            } else if ("clone".equals(method.getName())) {
                return EntityFactory.newInstance(entityClass, fields);
            }
        }

        throw new UnsupportedOperationException(method.toString());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + entityClass.hashCode();
        result = prime * result + fields.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || !Proxy.isProxyClass(obj.getClass())) {
            return false;
        }
        final InvocationHandler handler = Proxy.getInvocationHandler(obj);
        if (this == handler) {
            return true;
        }
        if (getClass() != handler.getClass()) {
            return false;
        }
        final EntityFactory<?> other = (EntityFactory<?>) handler;
        return entityClass.equals(other.entityClass) && fields.size() == other.fields.size() && fields.entrySet()
                .stream().allMatch(e -> Objects.deepEquals(e.getValue(), other.fields.get(e.getKey())));
    }

    @Override
    public String toString() {
        return entityClass.getName() + fields;
    }

    /**
     * 根据实体类来创建一个实例.
     *
     * @param entityClass 实体类
     * @param <E>         实体类
     * @return 实体类的一个实例
     */
    public static <E extends Entity> E newInstance(final Class<E> entityClass) {
        return newInstance(entityClass, null);
    }

    /**
     * 动态创建实体对象.
     *
     * @param entityClass 实体类
     * @param fields      属性集
     * @param <E>         实体类
     * @return 实体
     */
    @SuppressWarnings("unchecked")
    public static <E extends Entity> E newInstance(final Class<E> entityClass, final Map<String, Serializable> fields) {
        if (isEntityClass(entityClass)) {
            if (entityClass.isInterface()) {
                final Class<?> packageInterface = PackageUtils.getPackageInterface(entityClass, Package.class);
                return (E) Proxy.newProxyInstance(entityClass.getClassLoader(),
                        new Class[]{entityClass, PojoFields.class, EntityModifier.class, packageInterface},
                        new EntityFactory<>(entityClass, fields));
            } else {
                try {
                    return entityClass.getConstructor().newInstance();
                } catch (InvocationTargetException ex) {
                    throw new UnsupportedOperationException("Instantiation fails: " + entityClass, ex.getCause());
                } catch (Exception ex) {
                    throw new UnsupportedOperationException("Instantiation fails: " + entityClass, ex);
                }
            }
        } else {
            throw new UnsupportedOperationException("Invalid EntityClass: " + entityClass);
        }
    }

    /**
     * 判断是否有效的实体类.
     *
     * @param entityClass 实体类
     * @return 如果是一个有效的实体类，返回true，否则返回false
     */
    public static boolean isEntityClass(final Class<?> entityClass) {
        if (Entity.class.isAssignableFrom(entityClass)) {
            if (!Modifier.isAbstract(entityClass.getModifiers())) {
                if (entityClass.getAnnotation(Table.class) != null) {
                    try {
                        entityClass.getConstructor();
                    } catch (NoSuchMethodException | SecurityException e) {
                        return false;
                    }
                    return true;
                }
            } else if (entityClass.isInterface()) {
                return entityClass.getAnnotation(Table.class) != null;
            }
        }
        return false;
    }

    /**
     * 判断是否有效的接口型实体类.
     *
     * @param entityClass 实体类
     * @return 如果是一个有效的接口型实体类，返回true，否则返回false
     */
    public static boolean isEntityInterface(final Class<?> entityClass) {
        return isEntityClass(entityClass) && entityClass.isInterface();
    }

    /**
     * 判断是否有效的代理型实体类.
     *
     * @param proxyClass 代理类
     * @return 如果是一个有效的代理型实体类，返回true，否则返回false
     */
    public static boolean isEntityProxy(final Class<?> proxyClass) {
        return Proxy.isProxyClass(proxyClass) && PojoFields.class.isAssignableFrom(proxyClass)
                && isEntityInterface(getEntityClass(proxyClass));
    }

    /**
     * Clone an entity.
     *
     * @param proxy the entity to clone, null returns null
     * @param <E>   实体类
     * @return the clone of entity
     */
    @SuppressWarnings("unchecked")
    public static <E extends Entity> E clone(final E proxy) {
        try {
            if (proxy == null) {
                return null;
            } else if (proxy instanceof EntityModifier) {
                return (E) ((EntityModifier) proxy).clone();
            } else {
                return ObjectUtils.clone(proxy);
            }
        } catch (CloneNotSupportedException e) {
            throw new UnsupportedOperationException(e);
        } catch (CloneFailedException e) {
            throw new UnsupportedOperationException(e.getMessage(), e.getCause());
        }
    }

    /**
     * 获得代理对象的原始接口.
     *
     * @param proxy 代理对象
     * @param <E>   实体类
     * @return 代理对象的原始接口
     */
    @SuppressWarnings("unchecked")
    public static <E extends Entity> Class<E> getEntityClass(final E proxy) {
        if (Proxy.isProxyClass(proxy.getClass())) {
            final InvocationHandler handler = Proxy.getInvocationHandler(proxy);
            if (handler instanceof EntityFactory) {
                return ((EntityFactory<E>) handler).entityClass;
            }
        }
        return (Class<E>) proxy.getClass();
    }

    /**
     * 获得代理类的原始接口.
     *
     * @param proxyClass 代理类
     * @param <E>        实体类
     * @return 代理类的原始接口
     */
    @SuppressWarnings("unchecked")
    public static <E> Class<E> getEntityClass(final Class<E> proxyClass) {
        Class<E> c = proxyClass;
        if (Proxy.isProxyClass(proxyClass)) {
            for (Class<?> i : proxyClass.getInterfaces()) {
                if (isEntityInterface(i)) {
                    c = (Class<E>) i;
                }
            }
        }
        return c;
    }

    public interface PojoFields {
        /**
         * 获得实体中所有字段的map.
         *
         * @return 所有字段的map
         */
        Map<String, Object> fields();
    }
}
