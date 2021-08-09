package io.github.dbstarll.dubai.model.service.test;

import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.service.Service;

public interface TestServices<E extends Entity> extends Service<E> {
    void callTest(E entity);
}
