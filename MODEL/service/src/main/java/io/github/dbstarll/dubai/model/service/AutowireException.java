package io.github.dbstarll.dubai.model.service;

public class AutowireException extends RuntimeException {
    private static final long serialVersionUID = -8446056442461646306L;

    public AutowireException(String message) {
        super(message);
    }

    public AutowireException(Throwable cause) {
        super(cause);
    }
}
