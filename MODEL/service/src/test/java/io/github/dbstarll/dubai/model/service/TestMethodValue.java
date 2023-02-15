package io.github.dbstarll.dubai.model.service;

import io.github.dbstarll.dubai.model.service.attach.DefunctAttach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TestMethodValue {
    /**
     * 测试equals方法.
     *
     * @throws Exception Exception
     */
    @Test
    void testEquals() throws Exception {
        final Class<?> serviceInterface = DefunctAttach.class;
        final Method method = serviceInterface.getMethod("filterByDefunct", Boolean.TYPE);

        final MethodValue mv = new MethodValue(serviceInterface, method);

        assertEquals(mv, mv);
        assertFalse(mv.equals(null));
        assertNotEquals(mv, new Object());
    }
}
