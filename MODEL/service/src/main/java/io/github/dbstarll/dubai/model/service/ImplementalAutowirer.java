package io.github.dbstarll.dubai.model.service;

public interface ImplementalAutowirer {
    <I extends Implemental> void autowire(I implemental) throws AutowireException;
}
