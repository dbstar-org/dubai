package io.github.dbstarll.dubai.model.service.test;

import io.github.dbstarll.dubai.model.service.Implementation;

@Implementation(FailedImplemental.class)
public interface FailedAttach extends TestAttachs {
    void failed();
}
