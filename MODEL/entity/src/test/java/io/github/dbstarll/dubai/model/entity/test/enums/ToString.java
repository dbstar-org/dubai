package io.github.dbstarll.dubai.model.entity.test.enums;

import io.github.dbstarll.dubai.model.entity.EnumValue;

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
