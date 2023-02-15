package io.github.dbstarll.dubai.model.service;

import io.github.dbstarll.dubai.model.MongodTestCase;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.entity.info.Describable;
import io.github.dbstarll.dubai.model.service.test3.TestEntity;
import io.github.dbstarll.dubai.model.service.test3.TestService;
import io.github.dbstarll.dubai.model.service.validate.DefaultValidate;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestDescriptionValidation extends ServiceTestCase {
    private final Class<TestEntity> entityClass = TestEntity.class;
    private final Class<TestService> serviceClass = TestService.class;

    @BeforeAll
    public static void setup() {
        MongodTestCase.globalCollectionFactory();
    }

    @Test
    public void testInsertSetDescription() {
        useService(serviceClass, s -> {
            final TestEntity entity = EntityFactory.newInstance(entityClass);
            entity.setDescription("description");
            final DefaultValidate validate = new DefaultValidate();
            assertEquals(entity, s.save(entity, validate));
            assertFalse(validate.hasErrors());
        });
    }

    @Test
    public void testInsertSetDescriptionEmpty() {
        useService(serviceClass, s -> {
            final TestEntity entity = EntityFactory.newInstance(entityClass);
            entity.setDescription("    ");
            final DefaultValidate validate = new DefaultValidate();
            assertEquals(entity, s.save(entity, validate));
            assertFalse(validate.hasErrors());
            assertNull(entity.getDescription());
        });
    }

    @Test
    public void testInsertSetDescriptionLong() {
        useService(serviceClass, s -> {
            final TestEntity entity = EntityFactory.newInstance(entityClass);
            entity.setDescription(StringUtils.repeat("0123456789", 7));
            final DefaultValidate validate = new DefaultValidate();
            assertNull(s.save(entity, validate));
            assertTrue(validate.hasFieldErrors());
            assertEquals(1, validate.getFieldErrors().size());
            assertEquals(Collections.singletonList("备注不能超过 60 字符"),
                    validate.getFieldErrors().get(Describable.FIELD_NAME_DESCRIPTION));
        });
    }

    @Test
    public void testUpdateSetDescription() {
        useService(serviceClass, s -> {
            final TestEntity entity = EntityFactory.newInstance(entityClass);

            final DefaultValidate validate = new DefaultValidate();
            assertEquals(entity, s.save(entity, validate));
            assertFalse(validate.hasErrors());

            entity.setDescription("description");
            assertEquals(entity, s.save(entity, validate));
            assertFalse(validate.hasErrors());
        });
    }

    @Test
    public void testUpdateSetDescriptionSame() {
        useService(serviceClass, s -> {
            final TestEntity entity = EntityFactory.newInstance(entityClass);
            entity.setDescription("description");

            final DefaultValidate validate = new DefaultValidate();
            assertEquals(entity, s.save(entity, validate));
            assertFalse(validate.hasErrors());

            assertNull(s.save(entity, validate));
            assertFalse(validate.hasErrors());
        });
    }
}
