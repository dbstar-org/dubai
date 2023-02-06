package io.github.dbstarll.dubai.model.spring;

import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.service.Service;
import io.github.dbstarll.dubai.model.service.ServiceFactory;

public class ServiceBeanFactory {
    /**
     * 构造{@link io.github.dbstarll.dubai.model.service.Service}实例.
     *
     * @param serviceClass {@link io.github.dbstarll.dubai.model.service.Service}类
     * @param collection   {@link io.github.dbstarll.dubai.model.collection.Collection}实例
     * @param <E>          实体类
     * @param <S>          服务类
     * @return {@link io.github.dbstarll.dubai.model.service.Service}实例
     */
    public <E extends Entity, S extends Service<E>> S newInstance(final Class<S> serviceClass,
                                                                  final Collection<E> collection) {
        return ServiceFactory.newInstance(serviceClass, collection);
    }
}
