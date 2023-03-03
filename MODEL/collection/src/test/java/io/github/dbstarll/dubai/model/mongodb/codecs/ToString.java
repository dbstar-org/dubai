package io.github.dbstarll.dubai.model.mongodb.codecs;

import io.github.dbstarll.utils.lang.enums.EnumValue;

@EnumValue(method = "toString")
public enum ToString {
    ABD("abd"), DEF("def");

    private final String title;

    ToString(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}
