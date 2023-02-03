package io.github.dbstarll.dubai.model.entity.utils;

import io.github.dbstarll.dubai.model.entity.test.InterfaceEntity;
import org.apache.commons.beanutils.IntrospectionContext;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.beans.PropertyDescriptor;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class TestIntrospectionContextWrapper {
    private AtomicReference<String> result = new AtomicReference<>();
    private IntrospectionContext context;

    @Before
    public void setUp() {
        context = new IntrospectionContextWrapper(new IntrospectionContext() {
            @Override
            public void removePropertyDescriptor(String name) {
                result.set("removePropertyDescriptor: " + name);
            }

            @Override
            public Set<String> propertyNames() {
                result.set("propertyNames");
                return null;
            }

            @Override
            public boolean hasProperty(String name) {
                result.set("hasProperty: " + name);
                return false;
            }

            @Override
            public Class<?> getTargetClass() {
                result.set("getTargetClass");
                return null;
            }

            @Override
            public PropertyDescriptor getPropertyDescriptor(String name) {
                result.set("getPropertyDescriptor: " + name);
                return null;
            }

            @Override
            public void addPropertyDescriptors(PropertyDescriptor[] descriptors) {
                result.set("addPropertyDescriptors");
            }

            @Override
            public void addPropertyDescriptor(PropertyDescriptor desc) {
                result.set("addPropertyDescriptor");
            }
        }, InterfaceEntity.class);
    }

    @After
    public void tearDown() {
        this.context = null;
        this.result = null;
    }

    @Test
    public void testRemovePropertyDescriptor() {
        final String random = RandomStringUtils.random(10);
        context.removePropertyDescriptor(random);
        Assert.assertEquals("removePropertyDescriptor: " + random, result.get());
    }

    @Test
    public void testPropertyNames() {
        context.propertyNames();
        Assert.assertEquals("propertyNames", result.get());
    }

    @Test
    public void testHasProperty() {
        final String random = RandomStringUtils.random(10);
        context.hasProperty(random);
        Assert.assertEquals("hasProperty: " + random, result.get());
    }

    @Test
    public void testGetTargetClass() {
        Assert.assertEquals(InterfaceEntity.class, context.getTargetClass());
        Assert.assertNull(result.get());
    }

    @Test
    public void testGetPropertyDescriptor() {
        final String random = RandomStringUtils.random(10);
        context.getPropertyDescriptor(random);
        Assert.assertEquals("getPropertyDescriptor: " + random, result.get());
    }

    @Test
    public void testAddPropertyDescriptors() {
        context.addPropertyDescriptors(null);
        Assert.assertEquals("addPropertyDescriptors", result.get());
    }

    @Test
    public void testAddPropertyDescriptor() {
        context.addPropertyDescriptor(null);
        Assert.assertEquals("addPropertyDescriptor", result.get());
    }
}
