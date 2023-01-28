package io.github.dbstarll.dubai.model.entity.utils;

import org.apache.commons.beanutils.IntrospectionContext;

import java.beans.PropertyDescriptor;
import java.util.Set;

public final class IntrospectionContextWrapper implements IntrospectionContext {
    private final IntrospectionContext context;

    private final Class<?> targetClass;

    /**
     * 封装一个IntrospectionContext的代理.
     *
     * @param context     被代理的IntrospectionContext
     * @param targetClass 目标对象类
     */
    public IntrospectionContextWrapper(final IntrospectionContext context, final Class<?> targetClass) {
        this.context = context;
        this.targetClass = targetClass;
    }

    @Override
    public Class<?> getTargetClass() {
        return targetClass;
    }

    @Override
    public void addPropertyDescriptor(final PropertyDescriptor desc) {
        context.addPropertyDescriptor(desc);
    }

    @Override
    public void addPropertyDescriptors(final PropertyDescriptor[] descriptors) {
        context.addPropertyDescriptors(descriptors);
    }

    @Override
    public boolean hasProperty(final String name) {
        return context.hasProperty(name);
    }

    @Override
    public PropertyDescriptor getPropertyDescriptor(final String name) {
        return context.getPropertyDescriptor(name);
    }

    @Override
    public void removePropertyDescriptor(final String name) {
        context.removePropertyDescriptor(name);
    }

    @Override
    public Set<String> propertyNames() {
        return context.propertyNames();
    }
}
