package io.github.dbstarll.dubai.model.service;

import io.github.dbstarll.dubai.model.MongodTestCase;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.entity.info.Contentable;
import io.github.dbstarll.dubai.model.service.test3.contentable.TestContentableEntity;
import io.github.dbstarll.dubai.model.service.test3.contentable.TestContentableService;
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

class TestContentValidation extends ServiceTestCase {
    private final Class<TestContentableEntity> entityClass = TestContentableEntity.class;
    private final Class<TestContentableService> serviceClass = TestContentableService.class;

    @BeforeAll
    static void setup() {
        MongodTestCase.globalCollectionFactory();
    }

    @Test
    void testInsertOk() {
        useService(serviceClass, s -> {
            final TestContentableEntity entity = EntityFactory.newInstance(entityClass);
            entity.setContent("content".getBytes());
            entity.setContentType("text/plain");

            final DefaultValidate validate = new DefaultValidate();
            assertEquals(entity, s.save(entity, validate));
            assertFalse(validate.hasErrors());
        });
    }

    @Test
    void testInsertWithContentNotSet() {
        useService(serviceClass, s -> {
            final TestContentableEntity entity = EntityFactory.newInstance(entityClass);
            entity.setContentType("text/plain");
            final DefaultValidate validate = new DefaultValidate();
            assertNull(s.save(entity, validate));
            assertTrue(validate.hasFieldErrors());
            assertEquals(1, validate.getFieldErrors().size());
            assertEquals(Collections.singletonList("内容未设置"), validate.getFieldErrors().get(Contentable.FIELD_NAME_CONTENT));
        });
    }

    @Test
    void testInsertWithContentTypeError() {
        useService(serviceClass, s -> new HashMap<String, String>() {{
            put(null, "内容类型未设置");
            put("", "内容类型未设置");
            put("text", "不符合格式：主类型/子类型");
            put("/xml", "缺少主类型");
            put("text/", "缺少子类型");
            put("text/plain/xml", "只能有一个子类型");
        }}.forEach((k, v) -> {
            final TestContentableEntity entity = EntityFactory.newInstance(entityClass);
            entity.setContent("content".getBytes());
            entity.setContentType(k);
            final DefaultValidate valid = new DefaultValidate();
            assertNull(s.save(entity, valid));
            assertTrue(valid.hasFieldErrors());
            assertEquals(1, valid.getFieldErrors().size());
            assertEquals(Collections.singletonList(v), valid.getFieldErrors().get(Contentable.FIELD_NAME_CONTENT_TYPE));
        }));
    }

    @Test
    void testUpdateSetSameContentType() {
        useService(serviceClass, s -> {
            final TestContentableEntity entity = EntityFactory.newInstance(entityClass);
            entity.setContent("content".getBytes());
            entity.setContentType("text/plain");

            final DefaultValidate validate = new DefaultValidate();
            assertSame(entity, s.save(entity, validate));
            assertFalse(validate.hasErrors());

            assertNull(s.save(entity, validate));
            assertFalse(validate.hasErrors());
        });
    }

    @Test
    void testUpdateSetDiffContentType() {
        useService(serviceClass, s -> {
            final TestContentableEntity entity = EntityFactory.newInstance(entityClass);
            entity.setContent("content".getBytes());
            entity.setContentType("text/plain");

            final DefaultValidate validate = new DefaultValidate();
            assertSame(entity, s.save(entity, validate));
            assertFalse(validate.hasErrors());

            entity.setContentType("text/xml");
            assertSame(entity, s.save(entity, validate));
            assertFalse(validate.hasErrors());
        });
    }
}
