package io.github.dbstarll.dubai.model.service.test5;

import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.service.test.TestImplementals;

public final class ImplFailedImplemental extends TestImplementals<ImplFailedEntity, ImplFailedService>
        implements ImplFailedAttach {
    public ImplFailedImplemental(final ImplFailedService service,
                                 final Collection<ImplFailedEntity> collection) {
        super(service, collection);
        throw new RuntimeException("ImplFailed");
    }

    @Override
    public void done() {
        System.out.println("done");
    }
}
