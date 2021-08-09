package io.github.dbstarll.dubai.model.service.test;

import io.github.dbstarll.dubai.model.service.Implementation;

@Implementation(NotPublicImplemental.class)
public interface NotPublicAttach extends TestAttachs {
    void notPublic();
}
