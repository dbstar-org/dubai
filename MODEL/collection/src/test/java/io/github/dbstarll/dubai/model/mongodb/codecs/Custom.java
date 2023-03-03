package io.github.dbstarll.dubai.model.mongodb.codecs;

import io.github.dbstarll.utils.lang.enums.EnumValue;

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
