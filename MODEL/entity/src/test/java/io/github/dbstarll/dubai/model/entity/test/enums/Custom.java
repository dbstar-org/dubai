package io.github.dbstarll.dubai.model.entity.test.enums;

import io.github.dbstarll.dubai.model.entity.EnumValue;

@EnumValue(method = "getTitle")
public enum Custom {
    ABD("abd"), DEF("def");

    private final String title;

    Custom(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
