package io.github.dbstarll.dubai.model.entity.info;

import io.github.dbstarll.dubai.model.entity.InfoBase;
import org.bson.types.ObjectId;

import java.util.Map;

public interface Sourceable extends InfoBase {
    String FIELD_NAME_SOURCES = "sources";

    Map<String, ObjectId> getSources();

    void setSources(Map<String, ObjectId> sources);
}
