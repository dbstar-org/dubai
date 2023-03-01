package io.github.dbstarll.dubai.model.service;

import io.github.dbstarll.dubai.model.MongodTestCase;
import io.github.dbstarll.dubai.model.entity.Entity;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class ServiceTestCase extends MongodTestCase {
    protected final <E extends Entity, S extends Service<E>> void useService(
            final Class<S> serviceClass, final Consumer<S> consumer) {
        useService(serviceClass, s -> null, consumer);
    }

    protected final <E extends Entity, S extends Service<E>> void useService(
            final Class<S> serviceClass, final ImplementalAutowirer implementalAutowirer, final Consumer<S> consumer) {
        useService(serviceClass, s -> implementalAutowirer, consumer);
    }

    protected final <E extends Entity, S extends Service<E>> void useService(
            final Class<S> serviceClass, final Function<S, ImplementalAutowirer> function,
            final Consumer<S> consumer) {
        final Class<E> entityClass = ServiceFactory.getEntityClass(serviceClass);
        if (entityClass != null) {
            useCollection(entityClass, collection -> {
                final S service = ServiceFactory.newInstance(serviceClass, collection);
                if (service instanceof ImplementalAutowirerAware) {
                    ((ImplementalAutowirerAware) service).setImplementalAutowirer(function.apply(service));
                }
                consumer.accept(service);
            });
        }
    }
}
