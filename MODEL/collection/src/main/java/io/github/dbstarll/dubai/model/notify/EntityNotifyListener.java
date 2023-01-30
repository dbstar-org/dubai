package io.github.dbstarll.dubai.model.notify;

import io.github.dbstarll.dubai.model.entity.Entity;
import org.bson.types.ObjectId;

public interface EntityNotifyListener {
    /**
     * 接收到通知时的回调接口.
     *
     * @param entityClass 实体类
     * @param id          实体ID
     * @param notifyType  通知类型
     * @param companyId   实体所属的公司ID
     * @param clientId    发送通知的客户端ID
     * @param <E>         实体类
     */
    <E extends Entity> void onNotify(Class<E> entityClass, ObjectId id, NotifyType notifyType, ObjectId companyId,
                                     String clientId);
}
