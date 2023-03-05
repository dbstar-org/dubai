package io.github.dbstarll.dubai.model.service;

import io.github.dbstarll.dubai.model.service.attach.DefunctAttach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertTrue(Stream.of(null, new Object()).noneMatch(mv::equals));
        assertTrue(Stream.of(mv).allMatch(mv::equals));
    }
}
