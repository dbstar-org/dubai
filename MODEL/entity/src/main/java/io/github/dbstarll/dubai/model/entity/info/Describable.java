package io.github.dbstarll.dubai.model.entity.info;

import io.github.dbstarll.dubai.model.entity.InfoBase;

public interface Describable extends InfoBase {
    String FIELD_NAME_DESCRIPTION = "description";

    String getDescription();

    void setDescription(String description);
}
