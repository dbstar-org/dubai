package io.github.dbstarll.dubai.model.service;

public interface Implemental extends Base {
    /**
     * Invoked after it has set all bean properties supplied. This method allows the bean instance to
     * perform initialization only possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     */
    default void afterPropertiesSet() {
        // do nothing
    }
}
