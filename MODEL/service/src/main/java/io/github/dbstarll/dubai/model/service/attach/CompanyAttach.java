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
    /**
     * 按公司ID来过滤.
     *
     * @param companyId 公司ID
     * @return 过滤条件
     */
    Bson filterByCompanyId(ObjectId companyId);

    /**
     * 按公司ID来统计匹配的实体数量.
     *
     * @param companyId 公司ID
     * @return 匹配的实体数量
     */
    long countByCompanyId(ObjectId companyId);

    /**
     * 按公司ID来查询匹配的实体列表.
     *
     * @param companyId 公司ID
     * @return 匹配的实体列表
     */
    FindIterable<E> findByCompanyId(ObjectId companyId);

    /**
     * 按公司ID来删除所有匹配的实体.
     *
     * @param companyId 公司ID
     * @return 删除结果
     */
    DeleteResult deleteByCompanyId(ObjectId companyId);
}
