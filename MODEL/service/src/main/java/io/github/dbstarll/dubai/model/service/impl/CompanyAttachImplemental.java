package io.github.dbstarll.dubai.model.service.impl;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.result.DeleteResult;
import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.join.CompanyBase;
import io.github.dbstarll.dubai.model.service.Service;
import io.github.dbstarll.dubai.model.service.ServiceHelper;
import io.github.dbstarll.dubai.model.service.attach.CompanyAttach;
import io.github.dbstarll.utils.lang.wrapper.EntryWrapper;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import static com.mongodb.client.model.Filters.eq;

public final class CompanyAttachImplemental<E extends Entity & CompanyBase, S extends Service<E>>
        extends CoreImplementals<E, S> implements CompanyAttach<E> {
    /**
     * 构建CompanyAttachImplemental.
     *
     * @param service    service
     * @param collection collection
     */
    public CompanyAttachImplemental(final S service, final Collection<E> collection) {
        super(service, collection);
    }

    @Override
    public Bson filterByCompanyId(final ObjectId companyId) {
        return eq(CompanyBase.FIELD_NAME_COMPANY_ID, companyId);
    }

    @Override
    public long countByCompanyId(final ObjectId companyId) {
        return service.count(filterByCompanyId(companyId));
    }

    @Override
    public FindIterable<E> findByCompanyId(final ObjectId companyId) {
        return service.find(filterByCompanyId(companyId));
    }

    @Override
    public DeleteResult deleteByCompanyId(final ObjectId companyId) {
        return getCollection().deleteMany(filterByCompanyId(companyId));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <EOC extends Entity, SOC extends Service<EOC>> MongoIterable<Entry<E, EOC>> findWithCompany(
            final SOC companyService, final Bson filter) {
        final ServiceHelper<E, S> helper = (ServiceHelper<E, S>) service;
        final ServiceHelper<EOC, SOC> companyHelper = (ServiceHelper<EOC, SOC>) companyService;

        final List<Bson> pipeline = new LinkedList<>();
        final Bson matchFilter = aggregateMatchFilter(filter);
        if (matchFilter != null) {
            pipeline.add(Aggregates.match(matchFilter));
        }
        pipeline.add(Aggregates.lookup(companyHelper.getNamespace().getCollectionName(),
                CompanyBase.FIELD_NAME_COMPANY_ID, Entity.FIELD_NAME_ID, "_companies"));

        return getCollection().aggregate(pipeline, BsonDocument.class).map(t -> {
            final BsonArray companies = t.getArray("_companies");
            final E entity = helper.decode(t.asBsonReader(), DEFAULT_CONTEXT);
            final EOC company = companies.size() > 0
                    ? companyHelper.decode(companies.get(0).asDocument().asBsonReader(), DEFAULT_CONTEXT)
                    : null;
            return EntryWrapper.wrap(entity, company);
        });
    }
}
