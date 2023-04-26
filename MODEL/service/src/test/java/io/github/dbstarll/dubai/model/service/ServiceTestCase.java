package io.github.dbstarll.dubai.model.service;

import io.github.dbstarll.dubai.model.MongodTestCase;
import io.github.dbstarll.dubai.model.entity.Entity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class ServiceTestCase extends MongodTestCase {
    private BeanMap beanMap;
    private ImplementalAutowirer beanMapAutowirer;

    @BeforeEach
    public final void setupBeanMap() {
        this.beanMap = new BeanMap();
        this.beanMapAutowirer = new BeanMapImplementalAutowirer(beanMap);
    }

    @AfterEach
    public final void cleanupBeanMap() {
        this.beanMapAutowirer = null;
        this.beanMap = null;
    }

    protected final <T> T get(final Class<T> beanClass) {
        return beanMap.get(beanClass);
    }

    protected final <T> void put(final Class<T> beanClass, final T bean) {
        beanMap.put(beanClass, bean);
    }

    protected final <E extends Entity, S extends Service<E>> void useService(
            final Class<S> serviceClass, final Consumer<S> consumer) {
        useService(serviceClass, s -> beanMapAutowirer, consumer);
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
                put(serviceClass, service);
                if (service instanceof ImplementalAutowirerAware) {
                    ((ImplementalAutowirerAware) service).setImplementalAutowirer(function.apply(service));
                }
                consumer.accept(service);
            });
        }
    }
}
