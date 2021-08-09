package io.github.dbstarll.dubai.model.entity.test;

import org.bson.types.ObjectId;

import java.util.Date;

public class NoModifierEntity extends AbstractEntity {
    private static final long serialVersionUID = 3441218895538080786L;

    @Override
    public ObjectId getId() {
        return null;
    }

    @Override
    public Date getDateCreated() {
        return null;
    }

    @Override
    public Date getLastModified() {
        return null;
    }
}
