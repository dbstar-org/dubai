package io.github.dbstarll.dubai.model.service;

import io.github.dbstarll.dubai.model.MongodTestCase;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.entity.info.Sourceable;
import io.github.dbstarll.dubai.model.service.test3.TestEntity;
import io.github.dbstarll.dubai.model.service.test3.TestService;
import io.github.dbstarll.dubai.model.service.validate.DefaultValidate;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestSourceValidation extends ServiceTestCase {
    private final Class<TestEntity> entityClass = TestEntity.class;
    private final Class<TestService> serviceClass = TestService.class;

    @BeforeAll
    public static void setup() {
        MongodTestCase.globalCollectionFactory();
    }


    @Test
    public void testInsertOk() {
        useService(serviceClass, s -> {
            final TestEntity entity = EntityFactory.newInstance(entityClass);
            entity.setSources(Collections.singletonMap("abc", new ObjectId()));

            final DefaultValidate validate = new DefaultValidate();
            assertEquals(entity, s.save(entity, validate));
            assertFalse(validate.hasErrors());
        });
    }

    @Test
    public void testInsertWithEmptyKey() {
        useService(serviceClass, s -> {
            final TestEntity entity = EntityFactory.newInstance(entityClass);
            entity.setSources(Collections.singletonMap("", new ObjectId()));
            final DefaultValidate validate = new DefaultValidate();
            assertNull(s.save(entity, validate));
            assertEquals(Collections.singletonList("来源不能包含空的key或者value"),
                    validate.getFieldErrors().get(Sourceable.FIELD_NAME_SOURCES));
        });
    }

    @Test
    public void testInsertWithEmptyValue() {
        useService(serviceClass, s -> {
            final TestEntity entity = EntityFactory.newInstance(entityClass);
            entity.setSources(Collections.singletonMap("key", null));
            final DefaultValidate validate = new DefaultValidate();
            assertNull(s.save(entity, validate));
            assertEquals(Collections.singletonList("来源不能包含空的key或者value"),
                    validate.getFieldErrors().get(Sourceable.FIELD_NAME_SOURCES));
        });
    }
}
