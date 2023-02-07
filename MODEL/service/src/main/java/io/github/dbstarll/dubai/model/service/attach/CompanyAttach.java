package io.github.dbstarll.dubai.model.service.attach;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.result.DeleteResult;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.join.CompanyBase;
import io.github.dbstarll.dubai.model.service.Implementation;
import io.github.dbstarll.dubai.model.service.Service;
import io.github.dbstarll.dubai.model.service.impl.CompanyAttachImplemental;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.Map.Entry;

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

    /**
     * 与外部的公司表进行left join查询，返回实体与外部公司关联的结果列表.
     *
     * @param companyService 外部公司服务
     * @param filter         过滤条件
     * @param <EOC>          外部关联公司的实体类
     * @param <SOC>          外部关联公司的服务类
     * @return 实体与外部公司关联的结果列表
     */
    <EOC extends Entity, SOC extends Service<EOC>> MongoIterable<Entry<E, EOC>> findWithCompany(SOC companyService,
                                                                                                Bson filter);
}
