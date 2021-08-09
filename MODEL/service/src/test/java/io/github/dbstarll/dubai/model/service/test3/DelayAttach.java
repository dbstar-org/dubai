package io.github.dbstarll.dubai.model.service.test3;

import io.github.dbstarll.dubai.model.service.Implementation;
import io.github.dbstarll.dubai.model.service.test.TestAttachs;

@Implementation(DelayImplemental.class)
public interface DelayAttach extends TestAttachs {
    Object delay();
}
