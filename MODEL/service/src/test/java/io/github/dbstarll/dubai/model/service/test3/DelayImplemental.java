package io.github.dbstarll.dubai.model.service.test3;

import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.service.test.TestImplementals;

public final class DelayImplemental extends TestImplementals<TestEntity, TestService> implements DelayAttach {
    private final Object lock = new Object();

    public DelayImplemental(TestService service, Collection<TestEntity> collection) throws InterruptedException {
        super(service, collection);
        Thread.sleep(100);
    }

    @Override
    public Object delay() {
        return lock;
    }
}
