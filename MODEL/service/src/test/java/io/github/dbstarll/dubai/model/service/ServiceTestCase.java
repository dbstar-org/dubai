package io.github.dbstarll.dubai.model.service;

import io.github.dbstarll.dubai.model.MongodTestCase;
import io.github.dbstarll.dubai.model.entity.Entity;

import java.util.function.Consumer;

public abstract class ServiceTestCase extends MongodTestCase {
    protected final <E extends Entity, S extends Service<E>> void useService(final Class<S> serviceClass,
                                                                             final Consumer<S> consumer) {
        useService(serviceClass, null, consumer);
    }

    protected final <E extends Entity, S extends Service<E>> void useService(
            final Class<S> serviceClass, final ImplementalAutowirer implementalAutowirer, final Consumer<S> consumer) {
        final Class<E> entityClass = ServiceFactory.getEntityClass(serviceClass);
        if (entityClass != null) {
            useCollection(entityClass, collection -> {
                final S service = ServiceFactory.newInstance(serviceClass, collection);
                if (service instanceof ImplementalAutowirerAware) {
                    ((ImplementalAutowirerAware) service).setImplementalAutowirer(implementalAutowirer);
                }
                consumer.accept(service);
            });
        }
    }
}
