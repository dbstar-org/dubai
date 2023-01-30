package io.github.dbstarll.dubai.model.entity;

import org.bson.types.ObjectId;

import java.util.Date;

/**
 * 所有实体类的基类，此类中的属性不应由用户设置，而是系统自动设置.
 */
public interface Entity extends Base, Cloneable {
    String FIELD_NAME_ID = "_id";

    /**
     * 获得实体的id(唯一标识符).
     *
     * @return 实体的id
     */
    ObjectId getId();

    /**
     * 获得实体的创建时间.
     *
     * @return 创建时间
     */
    Date getDateCreated();

    /**
     * 获得实体的最后修改时间.
     *
     * @return 最后修改时间
     */
    Date getLastModified();
}
