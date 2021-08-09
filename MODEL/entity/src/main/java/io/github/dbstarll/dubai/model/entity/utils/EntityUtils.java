package io.github.dbstarll.dubai.model.entity.utils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class EntityUtils {
    private static final ConcurrentMap<Class<?>, Map<String, PropertyDescriptor>> PROPERTIES = new ConcurrentHashMap<>();

    public static PropertyDescriptor propertyDescriptor(Class<?> entityClass, String name) {
        return cachePropertyDescriptors(entityClass).get(name);
    }

    public static Iterable<PropertyDescriptor> propertyDescriptors(final Class<?> entityClass) {
        return cachePropertyDescriptors(entityClass).values();
    }

    public static Method getReadMethod(PropertyDescriptor descriptor) {
        return EntityPropertyUtilsBean.instance.getReadMethod(descriptor);
    }

    public static Method getWriteMethod(PropertyDescriptor descriptor) {
        return EntityPropertyUtilsBean.instance.getWriteMethod(descriptor);
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
        for (PropertyDescriptor pd : EntityPropertyUtilsBean.instance.getPropertyDescriptors(entityClass)) {
            pds.put(pd.getName(), pd);
        }
        return pds;
    }
}
