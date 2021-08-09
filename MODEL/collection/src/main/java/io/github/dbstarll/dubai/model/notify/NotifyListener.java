package io.github.dbstarll.dubai.model.notify;

public interface NotifyListener {
    void onNotify(String key, String value, NotifyParser parser);
}
