package io.github.dbstarll.dubai.model.entity.info;

import io.github.dbstarll.dubai.model.entity.InfoBase;

/**
 * 预置了内容字段的信息类实体.
 */
public interface Contentable extends InfoBase {
    String FIELD_NAME_CONTENT = "content";
    String FIELD_NAME_CONTENT_TYPE = "contentType";

    /**
     * 获得content.
     *
     * @return content
     */
    byte[] getContent();

    /**
     * 设置content.
     *
     * @param content 新的content
     */
    void setContent(byte[] content);

    /**
     * 获得contentType.
     *
     * @return contentType
     */
    String getContentType();

    /**
     * 设置contentType.
     *
     * @param contentType 新的contentType
     */
    void setContentType(String contentType);
}
