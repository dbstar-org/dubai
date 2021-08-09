package io.github.dbstarll.dubai.model.entity.test;

public class NoModifierCloneEntity extends NoModifierEntity {
    private static final long serialVersionUID = 8912357366827360613L;

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}
