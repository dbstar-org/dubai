package io.github.dbstarll.dubai.model.entity.join;

import io.github.dbstarll.dubai.model.entity.JoinBase;
import org.bson.types.ObjectId;

public interface CompanyBase extends JoinBase {
    String FIELD_NAME_COMPANY_ID = "companyId";

    /**
     * 获得companyId.
     *
     * @return companyId
     */
    ObjectId getCompanyId();

    /**
     * 设置companyId.
     *
     * @param companyId companyId
     */
    void setCompanyId(ObjectId companyId);
}
