package io.github.dbstarll.dubai.model.service.test;

import io.github.dbstarll.dubai.model.service.EntityService;
import io.github.dbstarll.dubai.model.service.attach.CompanyAttach;
import io.github.dbstarll.dubai.model.service.attach.DefunctAttach;

@EntityService
public interface TestEntityService
        extends MidService<String, TestEntity>, DefunctAttach<TestEntity>, CompanyAttach<TestEntity> {

}
