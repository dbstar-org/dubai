package io.github.dbstarll.dubai.model.service.test;

import io.github.dbstarll.dubai.model.service.EntityService;

@EntityService
public class ThrowClassService extends ClassService {
    public ThrowClassService() {
        throw new UnsupportedOperationException("ThrowClassService");
    }
}
