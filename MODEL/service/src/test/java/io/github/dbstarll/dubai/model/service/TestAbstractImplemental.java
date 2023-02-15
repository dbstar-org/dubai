package io.github.dbstarll.dubai.model.service;

import io.github.dbstarll.dubai.model.MongodTestCase;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.entity.EntityModifier;
import io.github.dbstarll.dubai.model.service.test3.TestEntity;
import io.github.dbstarll.dubai.model.service.test3.TestService;
import io.github.dbstarll.dubai.model.service.validate.DefaultValidate;
import io.github.dbstarll.dubai.model.service.validate.Validate;
import io.github.dbstarll.dubai.model.service.validate.ValidateException;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class TestAbstractImplemental extends ServiceTestCase {
    private final Class<TestEntity> entityClass = TestEntity.class;
    private final Class<TestService> serviceClass = TestService.class;

    @BeforeAll
    static void setup() {
        MongodTestCase.globalCollectionFactory();
    }

    @Test
    void testSaveValidateNull() {
        useService(serviceClass, s -> {
            final TestEntity entity = EntityFactory.newInstance(entityClass);
            entity.setDefunct(true);
            try {
                s.save(entity, null);
                fail("throw ValidateException");
            } catch (Throwable ex) {
                assertEquals(ValidateException.class, ex.getClass());
                final Validate validate = ((ValidateException) ex).getValidate();
                assertNotNull(validate);
                assertTrue(validate.hasErrors());
                assertTrue(validate.hasFieldErrors());
                assertFalse(validate.hasActionErrors());
            }
        });
    }

    @Test
    void testSaveEntityNull() {
        useService(serviceClass, s -> {
            final DefaultValidate validate = new DefaultValidate();
            assertNull(s.save(null, validate));
            assertTrue(validate.hasErrors());
            assertFalse(validate.hasFieldErrors());
            assertTrue(validate.hasActionErrors());
            assertEquals(Collections.singletonList("实体未设置"), validate.getActionErrors());
        });
    }

    @Test
    void testSaveEntityNotFound() {
        useService(serviceClass, s -> {
            final ObjectId id = new ObjectId();
            final TestEntity entity = EntityFactory.newInstance(entityClass);
            ((EntityModifier) entity).setId(id);
            final DefaultValidate validate = new DefaultValidate();
            assertNull(s.save(entity, validate));
            assertTrue(validate.hasErrors());
            assertFalse(validate.hasFieldErrors());
            assertTrue(validate.hasActionErrors());
            assertEquals(Collections.singletonList("实体未找到"), validate.getActionErrors());
        });
    }

    @Test
    void testSaveEntityNoChange() {
        useService(serviceClass, s -> {
            final TestEntity entity = EntityFactory.newInstance(entityClass);
            assertSame(entity, s.save(entity, null));
            final DefaultValidate validate = new DefaultValidate();
            assertNull(s.save(entity, validate));
            assertFalse(validate.hasErrors());
        });
    }

    @Test
    void testSaveOk() {
        useService(serviceClass, s -> {
            final TestEntity entity = EntityFactory.newInstance(entityClass);
            final DefaultValidate validate = new DefaultValidate();
            assertEquals(entity, s.save(entity, validate));
            assertFalse(validate.hasErrors());
        });
    }

    @Test
    void testSaveError() {
        useService(serviceClass, s -> {
            final TestEntity entity = EntityFactory.newInstance(entityClass);
            final DefaultValidate validate = new DefaultValidate();
            assertNull(s.saveFailed(entity, validate));
            assertTrue(validate.hasActionErrors());
            assertEquals(1, validate.getActionErrors().size());
            assertEquals("[SaveFailed]", validate.getActionErrors().toString());
        });
    }

    @Test
    void testSaveException() {
        useService(serviceClass, s -> {
            final TestEntity entity = EntityFactory.newInstance(entityClass);
            final DefaultValidate validate = new DefaultValidate();
            assertNull(s.saveException(entity, validate));
            assertTrue(validate.hasActionErrors());
            assertEquals(1, validate.getActionErrors().size());
            assertEquals("[SaveException]", validate.getActionErrors().toString());
        });
    }

    @Test
    void testNoGeneralValidateable() {
        useService(serviceClass, s -> {
            final InvocationHandler handler = Proxy.getInvocationHandler(s);
            final TestService service = (TestService) Proxy.newProxyInstance(TestService.class.getClassLoader(),
                    new Class[]{TestService.class, ImplementalAutowirerAware.class}, handler);
            final TestEntity entity = EntityFactory.newInstance(entityClass);
            entity.setDefunct(true);
            final DefaultValidate validate = new DefaultValidate();
            assertNotNull(service.save(entity, validate));
            assertFalse(validate.hasErrors());
        });
    }

    @Test
    void testDeleteByIdNotExist() {
        useService(serviceClass, s -> {
            final DefaultValidate validate = new DefaultValidate();
            assertNull(s.deleteById(new ObjectId(), validate));
            assertFalse(validate.hasErrors());
        });
    }

    @Test
    void testDeleteByIdOk() {
        useService(serviceClass, s -> {
            final TestEntity entity = EntityFactory.newInstance(entityClass);
            assertSame(entity, s.save(entity, null));

            final DefaultValidate validate = new DefaultValidate();
            entity.setDefunct(true);
            assertEquals(entity, s.deleteById(entity.getId(), validate));
            assertFalse(validate.hasErrors());
        });
    }

    @Test
    void testDeleteByIdFailed() {
        useService(serviceClass, s -> {
            final TestEntity entity = EntityFactory.newInstance(entityClass);
            assertSame(entity, s.save(entity, null));

            final DefaultValidate validate = new DefaultValidate();
            assertNull(s.deleteByIdFailed(new ObjectId(), validate));
            assertFalse(validate.hasErrors());

            assertNull(s.deleteByIdFailed(entity.getId(), validate));
            assertTrue(validate.hasActionErrors());
            assertEquals(1, validate.getActionErrors().size());
            assertEquals("[SaveFailed]", validate.getActionErrors().toString());
        });
    }

    @Test
    void testDeleteByIdException() {
        useService(serviceClass, s -> {
            final TestEntity entity = EntityFactory.newInstance(entityClass);
            assertSame(entity, s.save(entity, null));

            final DefaultValidate validate = new DefaultValidate();
            assertNull(s.deleteByIdException(new ObjectId(), validate));
            assertFalse(validate.hasErrors());

            assertNull(s.deleteByIdException(entity.getId(), validate));
            assertTrue(validate.hasActionErrors());
            assertEquals(1, validate.getActionErrors().size());
            assertEquals("[SaveException]", validate.getActionErrors().toString());
        });
    }

    @Test
    void testDeleteByIdValidateNull() {
        useService(serviceClass, s -> {
            final TestEntity entity = EntityFactory.newInstance(entityClass);
            assertSame(entity, s.save(entity, null));

            try {
                s.deleteByIdException(entity.getId(), null);
                fail("throw ValidateException");
            } catch (Throwable ex) {
                assertEquals(ValidateException.class, ex.getClass());
                final Validate validate = ((ValidateException) ex).getValidate();
                assertNotNull(validate);
                assertTrue(validate.hasErrors());
                assertFalse(validate.hasFieldErrors());
                assertTrue(validate.hasActionErrors());
                assertEquals(1, validate.getActionErrors().size());
                assertEquals("[SaveException]", validate.getActionErrors().toString());
            }
        });
    }
}
