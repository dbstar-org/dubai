package io.github.dbstarll.dubai.model.service.impl;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.info.Sourceable;
import io.github.dbstarll.dubai.model.service.Service;
import io.github.dbstarll.dubai.model.service.attach.SourceAttach;
import io.github.dbstarll.dubai.model.service.validate.Validate;
import io.github.dbstarll.dubai.model.service.validation.GeneralValidation;
import io.github.dbstarll.dubai.model.service.validation.Validation;
import org.apache.commons.lang3.StringUtils;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.mongodb.client.model.Filters.eq;
import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

public final class SourceAttachImplemental<E extends Entity & Sourceable, S extends Service<E>>
        extends CoreImplementals<E, S> implements SourceAttach<E> {
    /**
     * 构造SourceAttachImplemental.
     *
     * @param service    service
     * @param collection collection
     */
    public SourceAttachImplemental(final S service, final Collection<E> collection) {
        super(service, collection);
    }

    @Override
    public UpdateResult mergeSource(final String source, final ObjectId from, final ObjectId to) {
        return getCollection().updateMany(eq(Sourceable.FIELD_NAME_SOURCES + "." + notBlank(source), notNull(from)),
                Updates.set(Sourceable.FIELD_NAME_SOURCES + "." + source, notNull(to)));
    }

    @Override
    public UpdateResult updateSource(final ObjectId entityId, final Map<String, ObjectId> sources) {
        if (hasEmptyKeyOrValue(notNull(sources))) {
            throw new IllegalArgumentException("来源不能包含空的key或者value");
        }
        final List<Bson> updates = new ArrayList<>(sources.size());
        for (Entry<String, ObjectId> entry : sources.entrySet()) {
            updates.add(Updates.set(Sourceable.FIELD_NAME_SOURCES + "." + entry.getKey(), entry.getValue()));
        }
        return getCollection().updateMany(eq(notNull(entityId)), Updates.combine(updates));
    }

    @Override
    public UpdateResult removeSource(final ObjectId entityId, final Map<String, ObjectId> sources) {
        if (hasEmptyKeyOrValue(notNull(sources))) {
            throw new IllegalArgumentException("来源不能包含空的key或者value");
        }
        final List<Bson> filters = new ArrayList<>(sources.size() + 1);
        filters.add(eq(notNull(entityId)));
        final List<Bson> updates = new ArrayList<>(sources.size());

        for (Entry<String, ObjectId> entry : sources.entrySet()) {
            filters.add(eq(Sourceable.FIELD_NAME_SOURCES + "." + entry.getKey(), entry.getValue()));
            updates.add(Updates.unset(Sourceable.FIELD_NAME_SOURCES + "." + entry.getKey()));
        }

        return getCollection().updateMany(Filters.and(filters), Updates.combine(updates));
    }

    /**
     * sourceValidation.
     *
     * @return sourceValidation
     */
    @GeneralValidation(Sourceable.class)
    public Validation<E> sourceValidation() {
        return new AbstractBaseEntityValidation<Sourceable>(Sourceable.class) {
            @Override
            protected void validate(final Sourceable entity, final Sourceable original, final Validate validate) {
                if (hasEmptyKeyOrValue(entity.getSources())) {
                    validate.addFieldError(Sourceable.FIELD_NAME_SOURCES, "来源不能包含空的key或者value");
                }
            }
        };
    }

    private static boolean hasEmptyKeyOrValue(final Map<String, ObjectId> map) {
        if (map != null) {
            for (Entry<String, ObjectId> entry : map.entrySet()) {
                if (StringUtils.isBlank(entry.getKey()) || entry.getValue() == null) {
                    return true;
                }
            }
        }
        return false;
    }
}
