package io.github.dbstarll.dubai.model.entity.utils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class EntityUtils {
    private EntityUtils() {
        // 工具类禁止实例化
    }

    private static final Map<Class<?>, Map<String, PropertyDescriptor>> PROPERTIES = new ConcurrentHashMap<>();

    /**
     * 获得实体类中指定属性的描述符.
     *
     * @param entityClass 实体类
     * @param name        指定的属性名称
     * @return 属性的描述符
     */
    public static PropertyDescriptor propertyDescriptor(final Class<?> entityClass, final String name) {
        return cachePropertyDescriptors(entityClass).get(name);
    }

    /**
     * 获得实体类中所有属性的描述符的遍历.
     *
     * @param entityClass 实体类
     * @return 所有属性的描述符的遍历
     */
    public static Iterable<PropertyDescriptor> propertyDescriptors(final Class<?> entityClass) {
        return cachePropertyDescriptors(entityClass).values();
    }

    /**
     * 根据属性描述符获取属性的读方法.
     *
     * @param descriptor 属性描述符
     * @return 属性的读方法
     */
    public static Method getReadMethod(final PropertyDescriptor descriptor) {
        return EntityPropertyUtilsBean.INSTANCE.getReadMethod(descriptor);
    }

    /**
     * 根据属性描述符获取属性的写方法.
     *
     * @param descriptor 属性描述符
     * @return 属性的写方法
     */
    public static Method getWriteMethod(final PropertyDescriptor descriptor) {
        return EntityPropertyUtilsBean.INSTANCE.getWriteMethod(descriptor);
    }

    private static Map<String, PropertyDescriptor> cachePropertyDescriptors(final Class<?> entityClass) {
        final Map<String, PropertyDescriptor> pds = PROPERTIES.get(entityClass);
        if (pds != null) {
            return pds;
        } else {
            PROPERTIES.putIfAbsent(entityClass, getPropertyDescriptors(entityClass));
            return PROPERTIES.get(entityClass);
        }
    }

    private static Map<String, PropertyDescriptor> getPropertyDescriptors(final Class<?> entityClass) {
        final Map<String, PropertyDescriptor> pds = new HashMap<>();
        for (PropertyDescriptor pd : EntityPropertyUtilsBean.INSTANCE.getPropertyDescriptors(entityClass)) {
            pds.put(pd.getName(), pd);
        }
        return pds;
    }
}
