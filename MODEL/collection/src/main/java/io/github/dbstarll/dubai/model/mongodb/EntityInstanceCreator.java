package io.github.dbstarll.dubai.model.mongodb;

import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import org.bson.codecs.pojo.InstanceCreator;
import org.bson.codecs.pojo.PropertyModel;

class EntityInstanceCreator<E extends Entity> implements InstanceCreator<E> {
    private final E entity;

    EntityInstanceCreator(Class<E> entityClass) {
        this.entity = EntityFactory.newInstance(entityClass);
    }

    @Override
    public <S> void set(S value, PropertyModel<S> propertyModel) {
        propertyModel.getPropertyAccessor().set(entity, value);
    }

    @Override
    public E getInstance() {
        return entity;
    }
}