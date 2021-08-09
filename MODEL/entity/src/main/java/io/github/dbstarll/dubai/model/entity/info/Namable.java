package io.github.dbstarll.dubai.model.entity.info;

import io.github.dbstarll.dubai.model.entity.InfoBase;

public interface Namable extends InfoBase {
    String FIELD_NAME_NAME = "name";

    String getName();

    void setName(String name);
}
