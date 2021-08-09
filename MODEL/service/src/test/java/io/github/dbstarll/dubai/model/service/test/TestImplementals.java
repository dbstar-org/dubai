package io.github.dbstarll.dubai.model.service.test;

import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.service.AbstractImplemental;

public abstract class TestImplementals<E extends Entity, S extends TestServices<E>> extends AbstractImplemental<E, S> {
    public TestImplementals(S service, Collection<E> collection) {
        super(service, collection);
    }
}
