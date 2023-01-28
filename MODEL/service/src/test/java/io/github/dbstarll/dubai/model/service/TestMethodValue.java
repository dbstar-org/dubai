package io.github.dbstarll.dubai.model.service;

import io.github.dbstarll.dubai.model.service.attach.DefunctAttach;
import junit.framework.TestCase;

import java.lang.reflect.Method;

import static org.junit.Assert.assertNotEquals;

public class TestMethodValue extends TestCase {
    /**
     * 测试equals方法.
     *
     * @throws Exception Exception
     */
    public void testEquals() throws Exception {
        final Class<?> serviceInterface = DefunctAttach.class;
        final Method method = serviceInterface.getMethod("filterByDefunct", Boolean.TYPE);

        final MethodValue mv = new MethodValue(serviceInterface, method);

        assertEquals(mv, mv);
        assertNotEquals(mv, null);
        assertNotEquals(mv, new Object());
    }
}
