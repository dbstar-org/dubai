package io.github.dbstarll.dubai.model.service;

import io.github.dbstarll.dubai.model.MongodTestCase;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.entity.info.Namable;
import io.github.dbstarll.dubai.model.service.test3.namable.TestNamableEntity;
import io.github.dbstarll.dubai.model.service.test3.namable.TestNamableService;
import io.github.dbstarll.dubai.model.service.validate.DefaultValidate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class TestNameValidation extends ServiceTestCase {
    private final Class<TestNamableEntity> entityClass = TestNamableEntity.class;
    private final Class<TestNamableService> serviceClass = TestNamableService.class;

    @BeforeAll
    public static void setup() {
        MongodTestCase.globalCollectionFactory();
    }

    @Test
    void testInsertSetName() {
        useService(serviceClass, s -> {
            final TestNamableEntity entity = EntityFactory.newInstance(entityClass);
            entity.setName("name");
            final DefaultValidate validate = new DefaultValidate();
            assertSame(entity, s.save(entity, validate));
            assertFalse(validate.hasErrors());
        });
    }

    @Test
    void testInsertSetNameError() {
        useService(serviceClass, s -> new HashMap<String, String>() {{
            put(null, "名称未设置");
            put("", "名称未设置");
            put("\tname", "名称不能以空字符开头");
            put("name\t", "名称不能以空字符结尾");
            put("na\t\tme  jone", "名称不能包含连续的空字符");
            put("n", "名称不能少于 2 字符");
            put("01234567890123456789", "名称不能超过 16 字符");
        }}.forEach((k, v) -> {
            final TestNamableEntity entity = EntityFactory.newInstance(entityClass);
            entity.setName(k);
            final DefaultValidate validate = new DefaultValidate();
            assertNull(s.save(entity, validate));
            assertTrue(validate.hasFieldErrors());
            assertEquals(1, validate.getFieldErrors().size());
            assertEquals(Collections.singletonList(v), validate.getFieldErrors().get(Namable.FIELD_NAME_NAME));
        }));
    }

    @Test
    void testUpdateSetName() {
        useService(serviceClass, s -> {
            final TestNamableEntity entity = EntityFactory.newInstance(entityClass);
            entity.setName("name");

            final DefaultValidate validate = new DefaultValidate();
            assertSame(entity, s.save(entity, validate));
            assertFalse(validate.hasErrors());

            entity.setName("name2");
            assertSame(entity, s.save(entity, validate));
            assertFalse(validate.hasErrors());
        });
    }

    @Test
    void testUpdateSetNameSame() {
        useService(serviceClass, s -> {
            final TestNamableEntity entity = EntityFactory.newInstance(entityClass);
            entity.setName("name");

            final DefaultValidate validate = new DefaultValidate();
            assertSame(entity, s.save(entity, validate));
            assertFalse(validate.hasErrors());

            assertNull(s.save(entity, validate));
            assertFalse(validate.hasErrors());

        });
    }

    @Test
    void testNameValidation() {
        useService(serviceClass, s -> {
            try {
                s.name(5, 4);
                fail("throw IllegalArgumentException");
            } catch (Throwable ex) {
                assertEquals(IllegalArgumentException.class, ex.getClass());
                assertEquals("maxLength: 4 必须>= minLength: 5", ex.getMessage());
            }
        });
    }
}
