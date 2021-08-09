package io.github.dbstarll.dubai.model.service.test;

import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.entity.test.InterfaceEntity;

public class NotFinalImplemental extends TestImplementals<InterfaceEntity, InterfaceService> implements NotFinalAttach {
    public NotFinalImplemental(InterfaceService service, Collection<InterfaceEntity> collection) {
        super(service, collection);
    }

    @Override
    public void notFinal() {
    }
}
