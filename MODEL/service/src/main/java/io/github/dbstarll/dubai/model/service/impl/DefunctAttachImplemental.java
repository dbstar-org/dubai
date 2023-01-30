package io.github.dbstarll.dubai.model.service.impl;

import com.mongodb.client.FindIterable;
import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.func.Defunctable;
import io.github.dbstarll.dubai.model.service.Service;
import io.github.dbstarll.dubai.model.service.attach.DefunctAttach;
import io.github.dbstarll.dubai.model.service.validate.Validate;
import io.github.dbstarll.dubai.model.service.validation.GeneralValidation;
import io.github.dbstarll.dubai.model.service.validation.Validation;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public final class DefunctAttachImplemental<E extends Entity & Defunctable, S extends Service<E>>
        extends CoreImplementals<E, S> implements DefunctAttach<E> {
    /**
     * 构造DefunctAttachImplemental.
     *
     * @param service    service
     * @param collection collection
     */
    public DefunctAttachImplemental(final S service, final Collection<E> collection) {
        super(service, collection);
    }

    @Override
    public Bson filterByDefunct(final boolean defunct) {
        return eq(Defunctable.FIELD_NAME_DEFUNCT, defunct);
    }

    private Collection<E> getOriginalCollection() {
        return getCollection().original();
    }

    @Override
    public boolean contains(final ObjectId id, final Boolean defunct) {
        if (defunct == null) {
            return getOriginalCollection().contains(id);
        } else if (defunct) {
            return getOriginalCollection().count(and(eq(id), filterByDefunct(true))) > 0;
        } else {
            return service.contains(id);
        }
    }

    @Override
    public FindIterable<E> find(final Bson filter, final Boolean defunct) {
        if (defunct == null) {
            return getOriginalCollection().find(filter);
        } else if (defunct) {
            return getOriginalCollection().find(and(filter, filterByDefunct(true)));
        } else {
            return service.find(filter);
        }
    }

    @Override
    public E findById(final ObjectId id, final Boolean defunct) {
        if (defunct == null) {
            return getOriginalCollection().findById(id);
        } else if (defunct) {
            return getOriginalCollection().findOne(and(eq(id), filterByDefunct(true)));
        } else {
            return service.findById(id);
        }
    }

    @Override
    public long count(final Bson filter, final Boolean defunct) {
        if (defunct == null) {
            return getOriginalCollection().count(filter);
        } else if (defunct) {
            final Bson defunctFilter = filter == null ? filterByDefunct(true) : and(filter, filterByDefunct(true));
            return getOriginalCollection().count(defunctFilter);
        } else {
            return service.count(filter);
        }
    }

    /**
     * defunctValidation.
     *
     * @return defunctValidation
     */
    @GeneralValidation(Defunctable.class)
    public Validation<E> defunctValidation() {
        return new AbstractBaseEntityValidation<Defunctable>(Defunctable.class) {
            @Override
            protected void validate(final Defunctable entity, final Defunctable original, final Validate validate) {
                if (original == null ? entity.isDefunct() : entity.isDefunct() != original.isDefunct()) {
                    validate.addFieldError(Defunctable.FIELD_NAME_DEFUNCT, "defunct不允许外部修改");
                }
            }
        };
    }
}
