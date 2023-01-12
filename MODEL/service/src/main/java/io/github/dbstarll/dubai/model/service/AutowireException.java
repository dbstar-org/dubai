package io.github.dbstarll.dubai.model.service;

public class AutowireException extends RuntimeException {
    private static final long serialVersionUID = -8446056442461646306L;

    /**
     * 根据异常描述信息来构建AutowireException.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public AutowireException(final String message) {
        super(message);
    }

    /**
     * 根据根源异常来构建AutowireException.
     *
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link #getCause()} method).  (A <tt>null</tt> value is
     *              permitted, and indicates that the cause is nonexistent or
     *              unknown.)
     */
    public AutowireException(final Throwable cause) {
        super(cause);
    }
}
