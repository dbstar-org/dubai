package io.github.dbstarll.dubai.model.service.test5;

import io.github.dbstarll.dubai.model.service.Implementation;
import io.github.dbstarll.dubai.model.service.test.TestAttachs;

@Implementation(ImplFailedImplemental.class)
public interface ImplFailedAttach extends TestAttachs {
    void done();
}
