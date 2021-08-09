package io.github.dbstarll.dubai.model.notify;

import org.bson.types.ObjectId;

public interface NotifyParser {
    ObjectId getObjectId(String key);

    NotifyType getNotifyType();
}
