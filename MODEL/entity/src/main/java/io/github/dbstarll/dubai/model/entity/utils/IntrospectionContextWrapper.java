package io.github.dbstarll.dubai.model.entity.utils;

import org.apache.commons.beanutils.IntrospectionContext;

import java.beans.PropertyDescriptor;
import java.util.Set;

public class IntrospectionContextWrapper implements IntrospectionContext {
    private final IntrospectionContext context;

    private final Class<?> targetClass;

    public IntrospectionContextWrapper(IntrospectionContext context, Class<?> targetClass) {
        this.context = context;
        this.targetClass = targetClass;
    }

    @Override
    public Class<?> getTargetClass() {
        return targetClass;
    }

    public void addPropertyDescriptor(PropertyDescriptor desc) {
        context.addPropertyDescriptor(desc);
    }

    public void addPropertyDescriptors(PropertyDescriptor[] descriptors) {
        context.addPropertyDescriptors(descriptors);
    }

    public boolean hasProperty(String name) {
        return context.hasProperty(name);
    }

    public PropertyDescriptor getPropertyDescriptor(String name) {
        return context.getPropertyDescriptor(name);
    }

    public void removePropertyDescriptor(String name) {
        context.removePropertyDescriptor(name);
    }

    public Set<String> propertyNames() {
        return context.propertyNames();
    }
}