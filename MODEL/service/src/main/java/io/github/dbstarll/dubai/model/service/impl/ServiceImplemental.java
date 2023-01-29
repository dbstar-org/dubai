package io.github.dbstarll.dubai.model.service.impl;

import com.mongodb.client.FindIterable;
import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.service.Service;
import io.github.dbstarll.dubai.model.service.ServiceDeleter;
import io.github.dbstarll.dubai.model.service.ServiceSaver;
import io.github.dbstarll.dubai.model.service.validate.Validate;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

public final class ServiceImplemental<E extends Entity, S extends Service<E>> extends CoreImplementals<E, S>
        implements Service<E> {
    private final boolean serviceSaver;
    private final boolean serviceDeleter;

    /**
     * 构造ServiceImplemental.
     *
     * @param service    service
     * @param collection collection
     */
    public ServiceImplemental(final S service, final Collection<E> collection) {
        super(service, collection);
        this.serviceSaver = service instanceof ServiceSaver;
        this.serviceDeleter = service instanceof ServiceDeleter;
    }

    @Override
    public Class<E> getEntityClass() {
        return entityClass;
    }

    @Override
    public long count(final Bson filter) {
        return getCollection().count(filter);
    }

    @Override
    public boolean contains(final ObjectId id) {
        return getCollection().contains(id);
    }

    @Override
    public FindIterable<E> find(final Bson filter) {
        return getCollection().find(filter);
    }

    @Override
    public E findOne(final Bson filter) {
        return getCollection().findOne(filter);
    }

    @Override
    public E findById(final ObjectId id) {
        return getCollection().findById(id);
    }

    @Override
    public E deleteById(final ObjectId id) {
        return deleteById(id, null);
    }

    @SuppressWarnings("unchecked")
    private E deleteById(final ObjectId id, final Validate validate) {
        if (serviceDeleter) {
            return ((ServiceDeleter<E>) service).deleteById(id, validate);
        } else {
            return validateAndDelete(id, validate);
        }
    }

    @Override
    public E save(final E entity) {
        return save(entity, null, null);
    }

    @Override
    public E save(final E entity, final ObjectId newEntityId) {
        return save(entity, newEntityId, null);
    }

    @Override
    public E save(final E entity, final Validate validate) {
        return save(entity, null, validate);
    }

    @SuppressWarnings("unchecked")
    private E save(final E entity, final ObjectId newEntityId, final Validate validate) {
        if (serviceSaver) {
            return ((ServiceSaver<E>) service).save(entity, newEntityId, validate);
        } else {
            return validateAndSave(entity, newEntityId, validate);
        }
    }
}
