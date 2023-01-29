package io.github.dbstarll.dubai.model.service.attach;

import com.mongodb.client.result.UpdateResult;
import io.github.dbstarll.dubai.model.service.Implementation;
import io.github.dbstarll.dubai.model.service.impl.SourceAttachImplemental;
import org.bson.types.ObjectId;

import java.util.Map;

@Implementation(SourceAttachImplemental.class)
public interface SourceAttach extends CoreAttachs {
    /**
     * 合并来源字段，将指定来源中的from替换为to.
     *
     * @param source 单个来源
     * @param from   源来源ID
     * @param to     更新后的来源ID
     * @return 更新结果
     */
    UpdateResult mergeSource(String source, ObjectId from, ObjectId to);

    /**
     * 添加/替换新的来源字段中的所有信息，到指定实体ID对应的实体对象中.
     *
     * @param entityId 实体ID
     * @param sources  待新增的来源字段
     * @return 更新结果
     */
    UpdateResult updateSource(ObjectId entityId, Map<String, ObjectId> sources);

    /**
     * 删除指定实体ID对应的实体对象的与指定来源字段相匹配的内容.
     *
     * @param entityId 实体ID
     * @param sources  待删除的来源字段
     * @return 更新结果
     */
    UpdateResult removeSource(ObjectId entityId, Map<String, ObjectId> sources);
}
