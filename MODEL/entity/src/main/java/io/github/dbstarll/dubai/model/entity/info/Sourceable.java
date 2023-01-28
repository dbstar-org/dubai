package io.github.dbstarll.dubai.model.entity.info;

import io.github.dbstarll.dubai.model.entity.InfoBase;
import org.bson.types.ObjectId;

import java.util.Map;

/**
 * 预置了实体来源字段的信息类实体.
 */
public interface Sourceable extends InfoBase {
    String FIELD_NAME_SOURCES = "sources";

    /**
     * 获得实体来源.
     *
     * @return 实体来源
     */
    Map<String, ObjectId> getSources();

    /**
     * 设置实体来源.
     *
     * @param sources 新的实体来源
     */
    void setSources(Map<String, ObjectId> sources);
}
