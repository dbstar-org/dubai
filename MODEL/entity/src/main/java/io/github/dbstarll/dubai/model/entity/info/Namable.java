package io.github.dbstarll.dubai.model.entity.info;

import io.github.dbstarll.dubai.model.entity.InfoBase;

/**
 * 预置了名字字段的信息类实体.
 */
public interface Namable extends InfoBase {
    String FIELD_NAME_NAME = "name";

    /**
     * 获得名字.
     *
     * @return 名字
     */
    String getName();

    /**
     * 设置名字.
     *
     * @param name 新的名字
     */
    void setName(String name);
}
