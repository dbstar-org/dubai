package io.github.dbstarll.dubai.model.notify;

import io.github.dbstarll.dubai.model.entity.Entity;

public interface NotifyProvider {
    /**
     * 发送实体消息通知.
     *
     * @param e          实体对象
     * @param notifyType 通知类型
     * @param <E>        实体类
     */
    <E extends Entity> void doNotify(E e, NotifyType notifyType);
}
