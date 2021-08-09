package io.github.dbstarll.dubai.model.service.impl;

import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.service.AbstractImplemental;
import io.github.dbstarll.dubai.model.service.Service;

public abstract class CoreImplementals<E extends Entity, S extends Service<E>> extends AbstractImplemental<E, S> {
    public CoreImplementals(S service, Collection<E> collection) {
        super(service, collection);
    }
}
