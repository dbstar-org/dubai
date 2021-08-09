package io.github.dbstarll.dubai.model.entity.join;

import io.github.dbstarll.dubai.model.entity.JoinBase;
import org.bson.types.ObjectId;

public interface CompanyBase extends JoinBase {
    String FIELD_NAME_COMPANY_ID = "companyId";

    ObjectId getCompanyId();

    void setCompanyId(ObjectId companyId);
}
