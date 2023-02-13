package io.github.dbstarll.dubai.model.service;

import io.github.dbstarll.dubai.model.service.test5.ImplFailedImplemental;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ImplementalInstanceExceptionTest {
    @Test
    public void testCause() {
        final Exception e = new ImplementalInstanceException(ImplFailedImplemental.class,
                new IllegalArgumentException());
        assertNotNull(e.getCause());
        assertEquals(IllegalArgumentException.class, e.getCause().getClass());
    }

    @Test
    public void testInvocationTargetExceptionCause() {
        final Exception e = new ImplementalInstanceException(ImplFailedImplemental.class,
                new InvocationTargetException(new IllegalAccessException()));
        assertNotNull(e.getCause());
        assertEquals(IllegalAccessException.class, e.getCause().getClass());
    }
}