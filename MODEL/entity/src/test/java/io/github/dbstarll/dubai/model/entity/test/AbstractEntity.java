package io.github.dbstarll.dubai.model.entity.test;

import io.github.dbstarll.dubai.model.entity.Entity;

public abstract class AbstractEntity implements Entity {
    private static final long serialVersionUID = -2782048515969852427L;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
