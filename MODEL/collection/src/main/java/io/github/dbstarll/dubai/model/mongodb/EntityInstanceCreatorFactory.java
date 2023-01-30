package io.github.dbstarll.dubai.model.mongodb;

import io.github.dbstarll.dubai.model.entity.Entity;
import org.bson.codecs.pojo.InstanceCreator;
import org.bson.codecs.pojo.InstanceCreatorFactory;

public class EntityInstanceCreatorFactory<E extends Entity> implements InstanceCreatorFactory<E> {
    private final Class<E> entityClass;

    /**
     * 构造一个EntityInstanceCreator的工厂类.
     *
     * @param pojoInterface 实体类接口
     */
    public EntityInstanceCreatorFactory(final Class<E> pojoInterface) {
        this.entityClass = pojoInterface;
    }

    @Override
    public InstanceCreator<E> create() {
        return new EntityInstanceCreator<>(entityClass);
    }
}
