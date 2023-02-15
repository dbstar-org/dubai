package io.github.dbstarll.dubai.model.entity.utils;

import io.github.dbstarll.dubai.model.entity.test.InterfaceEntity;
import org.apache.commons.beanutils.IntrospectionContext;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.beans.PropertyDescriptor;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TestIntrospectionContextWrapper {
    private AtomicReference<String> result = new AtomicReference<>();
    private IntrospectionContext context;

    @BeforeEach
    void setUp() {
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

    @AfterEach
    void tearDown() {
        this.context = null;
        this.result = null;
    }

    @Test
    void testRemovePropertyDescriptor() {
        final String random = RandomStringUtils.random(10);
        context.removePropertyDescriptor(random);
        assertEquals("removePropertyDescriptor: " + random, result.get());
    }

    @Test
    void testPropertyNames() {
        context.propertyNames();
        assertEquals("propertyNames", result.get());
    }

    @Test
    void testHasProperty() {
        final String random = RandomStringUtils.random(10);
        context.hasProperty(random);
        assertEquals("hasProperty: " + random, result.get());
    }

    @Test
    void testGetTargetClass() {
        assertEquals(InterfaceEntity.class, context.getTargetClass());
        assertNull(result.get());
    }

    @Test
    void testGetPropertyDescriptor() {
        final String random = RandomStringUtils.random(10);
        context.getPropertyDescriptor(random);
        assertEquals("getPropertyDescriptor: " + random, result.get());
    }

    @Test
    void testAddPropertyDescriptors() {
        context.addPropertyDescriptors(null);
        assertEquals("addPropertyDescriptors", result.get());
    }

    @Test
    void testAddPropertyDescriptor() {
        context.addPropertyDescriptor(null);
        assertEquals("addPropertyDescriptor", result.get());
    }
}
