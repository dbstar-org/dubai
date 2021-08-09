package io.github.dbstarll.dubai.model.entity.info;

import io.github.dbstarll.dubai.model.entity.InfoBase;

public interface Contentable extends InfoBase {
    String FIELD_NAME_CONTENT = "content";
    String FIELD_NAME_CONTENT_TYPE = "contentType";

    byte[] getContent();

    void setContent(byte[] content);

    String getContentType();

    void setContentType(String contentType);
}
