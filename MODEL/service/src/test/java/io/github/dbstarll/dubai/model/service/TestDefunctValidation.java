package io.github.dbstarll.dubai.model.service;

import io.github.dbstarll.dubai.model.MongodTestCase;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.entity.func.Defunctable;
import io.github.dbstarll.dubai.model.service.test3.TestEntity;
import io.github.dbstarll.dubai.model.service.test3.TestService;
import io.github.dbstarll.dubai.model.service.validate.DefaultValidate;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TestDefunctValidation extends ServiceTestCase {
    private final Class<TestEntity> entityClass = TestEntity.class;
    private final Class<TestService> serviceClass = TestService.class;

    @BeforeClass
    public static void setup() {
        MongodTestCase.globalCollectionFactory();
    }

    @Test
    public void testInsertSetDefunct() {
        useService(serviceClass, s -> {
            final TestEntity entity = EntityFactory.newInstance(entityClass);
            entity.setDefunct(true);
            final DefaultValidate validate = new DefaultValidate();
            assertNull(s.save(entity, validate));
            assertTrue(validate.hasFieldErrors());
            assertEquals(1, validate.getFieldErrors().size());
            assertEquals(Collections.singletonList("defunct不允许外部修改"),
                    validate.getFieldErrors().get(Defunctable.FIELD_NAME_DEFUNCT));
        });
    }

    @Test
    public void testUpdateSetDefunct() {
        useService(serviceClass, s -> {
            final TestEntity entity = EntityFactory.newInstance(entityClass);

            final DefaultValidate validate = new DefaultValidate();
            assertSame(entity, s.save(entity, validate));
            assertFalse(validate.hasFieldErrors());

            entity.setDefunct(true);
            assertNull(s.save(entity, validate));
            assertTrue(validate.hasFieldErrors());
            assertEquals(1, validate.getFieldErrors().size());
            assertEquals(Collections.singletonList("defunct不允许外部修改"),
                    validate.getFieldErrors().get(Defunctable.FIELD_NAME_DEFUNCT));
        });
    }
}
