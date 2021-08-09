package io.github.dbstarll.dubai.model.entity;

import org.bson.types.ObjectId;

import java.util.Date;

public interface Entity extends Base, Cloneable {
    String FIELD_NAME_ID = "_id";

    ObjectId getId();

    Date getDateCreated();

    Date getLastModified();
}
