package io.github.dbstarll.dubai.model.service.attach;

import com.mongodb.client.FindIterable;
import com.mongodb.client.result.DeleteResult;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.join.CompanyBase;
import io.github.dbstarll.dubai.model.service.Implementation;
import io.github.dbstarll.dubai.model.service.impl.CompanyAttachImplemental;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

@Implementation(CompanyAttachImplemental.class)
public interface CompanyAttach<E extends Entity & CompanyBase> extends CoreAttachs {
    Bson filterByCompanyId(ObjectId companyId);

    long countByCompanyId(ObjectId companyId);

    FindIterable<E> findByCompanyId(ObjectId companyId);

    DeleteResult deleteByCompanyId(ObjectId companyId);
}
