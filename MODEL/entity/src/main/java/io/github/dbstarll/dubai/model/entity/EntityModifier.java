package io.github.dbstarll.dubai.model.entity;

import org.bson.types.ObjectId;

import java.util.Date;

/**
 * 对实体基类属性的修改接口.
 */
public interface EntityModifier extends FunctionalBase {
    /**
     * 设置实体的id(唯一标识符).
     *
     * @param id 新的id
     */
    void setId(ObjectId id);

    /**
     * 设置实体的创建时间.
     *
     * @param dateCreated 新的创建时间
     */
    void setDateCreated(Date dateCreated);

    /**
     * 设置实体的最后修改时间.
     *
     * @param lastModified 新的最后修改时间
     */
    void setLastModified(Date lastModified);

    /**
     * 克隆实体.
     *
     * @return 克隆后的新实体.
     * @throws CloneNotSupportedException 不支持克隆操作时抛出
     */
    Object clone() throws CloneNotSupportedException;
}
