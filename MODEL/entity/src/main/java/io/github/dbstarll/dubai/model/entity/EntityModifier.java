package io.github.dbstarll.dubai.model.entity;

import org.bson.types.ObjectId;

import java.util.Date;

public interface EntityModifier extends FunctionalBase {
    void setId(ObjectId id);

    void setDateCreated(Date dateCreated);

    void setLastModified(Date lastModified);

    Object clone() throws CloneNotSupportedException;
}
