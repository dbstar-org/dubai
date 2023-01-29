package io.github.dbstarll.dubai.model.service;

public interface ImplementalAutowirer {
    /**
     * 自动装配Implemental实例.
     *
     * @param implemental Implemental实例
     * @param <I>         Implemental类
     * @throws AutowireException 自动装配异常
     */
    <I extends Implemental> void autowire(I implemental) throws AutowireException;
}
