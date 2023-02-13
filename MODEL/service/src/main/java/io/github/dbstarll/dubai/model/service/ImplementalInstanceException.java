package io.github.dbstarll.dubai.model.service;

import java.lang.reflect.InvocationTargetException;

public class ImplementalInstanceException extends RuntimeException {
    /**
     * 构建ImplementalInstanceException.
     *
     * @param implementalClass 实例化失败的Implemental类
     * @param cause            the cause (which is saved for later retrieval by the
     *                         {@link #getCause()} method).  (A <tt>null</tt> value is
     *                         permitted, and indicates that the cause is nonexistent or
     *                         unknown.)
     */
    public ImplementalInstanceException(final Class<?> implementalClass, final Throwable cause) {
        super("不能实例化Implemental：" + implementalClass.getName(), parseCause(cause));
    }

    private static Throwable parseCause(final Throwable cause) {
        if (cause instanceof InvocationTargetException) {
            return ((InvocationTargetException) cause).getTargetException();
        }
        return cause;
    }
}
