package io.github.dbstarll.dubai.model.service.test;

import io.github.dbstarll.dubai.model.service.Implementation;

@Implementation(NotFinalImplemental.class)
public interface NotFinalAttach extends TestAttachs {
    void notFinal();
}
