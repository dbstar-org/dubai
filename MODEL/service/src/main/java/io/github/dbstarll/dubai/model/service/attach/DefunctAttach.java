package io.github.dbstarll.dubai.model.service.attach;

import com.mongodb.client.FindIterable;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.func.Defunctable;
import io.github.dbstarll.dubai.model.service.Implementation;
import io.github.dbstarll.dubai.model.service.impl.DefunctAttachImplemental;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

@Implementation(DefunctAttachImplemental.class)
public interface DefunctAttach<E extends Entity & Defunctable> extends CoreAttachs {
    /**
     * 按实体的伪删除标记来生成查询条件.
     *
     * @param defunct 是否已经删除
     * @return 查询条件
     */
    Bson filterByDefunct(boolean defunct);

    /**
     * 按伪删除标记来检测是否包含实体ID对应的实体.
     * <p>
     * defunct == null：检测所有实体
     * defunct == true：检测所有已伪删除的实体
     * defunct == false：检测所有未伪删除的实体
     * </p>
     *
     * @param id      实体ID
     * @param defunct 伪删除标记
     * @return 是否包含
     */
    boolean contains(ObjectId id, Boolean defunct);

    /**
     * 在原查询条件的基础上，按伪删除标记来叠加查询条件，并获得查询结果.
     * <p>
     * defunct == null：检测所有实体
     * defunct == true：检测所有已伪删除的实体
     * defunct == false：检测所有未伪删除的实体
     * </p>
     *
     * @param filter  原查询条件
     * @param defunct 伪删除标记
     * @return 查询结果
     */
    FindIterable<E> find(Bson filter, Boolean defunct);

    /**
     * 按伪删除标记来获取实体ID对应的实体.
     * <p>
     * defunct == null：检测所有实体
     * defunct == true：检测所有已伪删除的实体
     * defunct == false：检测所有未伪删除的实体
     * </p>
     *
     * @param id      实体ID
     * @param defunct 伪删除标记
     * @return 实体ID对应的实体
     */
    E findById(ObjectId id, Boolean defunct);

    /**
     * 在原查询条件的基础上，按伪删除标记来叠加查询条件，并获得匹配记录数量.
     * <p>
     * defunct == null：检测所有实体
     * defunct == true：检测所有已伪删除的实体
     * defunct == false：检测所有未伪删除的实体
     * </p>
     *
     * @param filter  原查询条件
     * @param defunct 伪删除标记
     * @return 匹配记录数量
     */
    long count(Bson filter, Boolean defunct);
}
