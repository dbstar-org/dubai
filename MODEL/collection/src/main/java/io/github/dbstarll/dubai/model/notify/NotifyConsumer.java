package io.github.dbstarll.dubai.model.notify;

public interface NotifyConsumer {
    void regist(NotifyListener listener);

    void unRegist(NotifyListener listener);
}
