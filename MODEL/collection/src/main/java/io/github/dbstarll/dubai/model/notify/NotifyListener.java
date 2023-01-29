package io.github.dbstarll.dubai.model.notify;

public interface NotifyListener {
    /**
     * 在收到通知时回调.
     *
     * @param key    消息key
     * @param value  消息value
     * @param parser 消息解析器
     */
    void onNotify(String key, String value, NotifyParser parser);
}
