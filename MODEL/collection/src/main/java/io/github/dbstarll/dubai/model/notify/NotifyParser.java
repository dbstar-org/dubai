package io.github.dbstarll.dubai.model.notify;

import org.bson.types.ObjectId;

public interface NotifyParser {
    /**
     * 从消息中解析关联的ID.
     *
     * @param key ID对应的关联key
     * @return 关联的ID
     */
    ObjectId getObjectId(String key);

    /**
     * 从消息中解析通知类型.
     *
     * @return 通知类型
     */
    NotifyType getNotifyType();
}
