package io.github.dbstarll.dubai.model.entity.info;

import io.github.dbstarll.dubai.model.entity.InfoBase;

/**
 * 预置了描述信息的信息类实体.
 */
public interface Describable extends InfoBase {
    String FIELD_NAME_DESCRIPTION = "description";

    /**
     * 获得描述信息.
     *
     * @return 描述信息
     */
    String getDescription();

    /**
     * 设置描述信息.
     *
     * @param description 新的描述信息
     */
    void setDescription(String description);
}
