package io.github.dbstarll.dubai.model.entity.test;

import io.github.dbstarll.dubai.model.entity.Table;
import io.github.dbstarll.dubai.model.entity.func.Cacheable;

@Table
public class NoCloneEntity extends ClassEntity implements Cacheable {
    private static final long serialVersionUID = 8608703309692388732L;

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}