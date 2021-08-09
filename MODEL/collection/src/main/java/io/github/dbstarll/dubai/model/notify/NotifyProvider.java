package io.github.dbstarll.dubai.model.notify;

import io.github.dbstarll.dubai.model.entity.Entity;

public interface NotifyProvider {
    <E extends Entity> void doNotify(E e, NotifyType notifyType);
}
