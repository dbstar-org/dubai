package io.github.dbstarll.dubai.model.entity.test;

import io.github.dbstarll.dubai.model.entity.Table;

@Table
public class ThrowClassEntity extends ClassEntity {
    private static final long serialVersionUID = -4710404662616946976L;

    public ThrowClassEntity() {
        throw new UnsupportedOperationException("ThrowClassEntity");
    }
}
