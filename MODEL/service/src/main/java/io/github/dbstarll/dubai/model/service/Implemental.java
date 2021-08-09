package io.github.dbstarll.dubai.model.service;

public interface Implemental extends Base {

    /**
     * Invoked after it has set all bean properties supplied. This method allows the bean instance to
     * perform initialization only possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such as failure to set an essential
     *                   property) or if initialization fails.
     */
    void afterPropertiesSet() throws Exception;
}
