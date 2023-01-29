package io.github.dbstarll.dubai.model.service.impl;

import com.mongodb.client.FindIterable;
import com.mongodb.client.result.DeleteResult;
import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.join.CompanyBase;
import io.github.dbstarll.dubai.model.service.Service;
import io.github.dbstarll.dubai.model.service.attach.CompanyAttach;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

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
}
