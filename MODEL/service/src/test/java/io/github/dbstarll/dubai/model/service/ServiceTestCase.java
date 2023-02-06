package io.github.dbstarll.dubai.model.service;

import io.github.dbstarll.dubai.model.MongodTestCase;
import io.github.dbstarll.dubai.model.entity.Entity;

import java.util.function.Consumer;

import static org.junit.Assert.assertNotNull;

public abstract class ServiceTestCase extends MongodTestCase {
    protected final <E extends Entity, S extends Service<E>> void useService(final Class<S> serviceClass,
                                                                             final Consumer<S> consumer) {
        final Class<E> entityClass = ServiceFactory.getEntityClass(serviceClass);
        assertNotNull(entityClass);
        useCollection(entityClass, collection -> consumer.accept(ServiceFactory.newInstance(serviceClass, collection)));
    }
}
