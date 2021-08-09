package io.github.dbstarll.dubai.model.service.test;

import io.github.dbstarll.dubai.model.entity.test.InterfaceEntity;
import io.github.dbstarll.dubai.model.service.EntityService;
import io.github.dbstarll.dubai.model.service.attach.DefunctAttach;

@EntityService
public interface InterfaceService extends TestServices<InterfaceEntity>, InterfaceServiceAttach, FailedAttach,
        NotPublicAttach, NotFinalAttach, DefunctAttach<InterfaceEntity> {
    void unImplementation();
}
