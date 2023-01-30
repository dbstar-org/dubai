package io.github.dbstarll.dubai.model.service.test3;

import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.service.test.TestImplementals;

public final class DelayImplemental extends TestImplementals<TestEntity, TestService> implements DelayAttach {
    private final Object lock = new Object();

    public DelayImplemental(TestService service, Collection<TestEntity> collection) {
        super(service, collection);
    }

    @Override
    public Object delay() {
        return lock;
    }
}
