package io.github.dbstarll.dubai.model.notify;

public interface NotifyConsumer {
    /**
     * 注册通知监听器.
     *
     * @param listener 通知监听器
     */
    void register(NotifyListener listener);

    /**
     * 注销通知监听器.
     *
     * @param listener 通知监听器
     */
    void unRegister(NotifyListener listener);
}
