package io.github.dbstarll.dubai.model.notify;

import io.github.dbstarll.dubai.model.entity.Entity;
import org.bson.types.ObjectId;

public interface EntityNotifyListener {
    <E extends Entity> void onNotify(Class<E> entityClass, ObjectId id, NotifyType notifyType, ObjectId companyId,
                                     String clientId);
}
