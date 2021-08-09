package io.github.dbstarll.dubai.model.service.test;

import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.entity.test.InterfaceEntity;

final class NotPublicImplemental extends TestImplementals<InterfaceEntity, InterfaceService>
        implements NotPublicAttach {
    public NotPublicImplemental(InterfaceService service, Collection<InterfaceEntity> collection) {
        super(service, collection);
    }

    @Override
    public void notPublic() {
    }
}
