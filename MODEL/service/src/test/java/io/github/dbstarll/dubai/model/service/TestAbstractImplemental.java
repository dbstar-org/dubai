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
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestAbstractImplemental extends ServiceTestCase {
    private final Class<TestEntity> entityClass = TestEntity.class;
    private final Class<TestService> serviceClass = TestService.class;

    @BeforeClass
    public static void setup() {
        MongodTestCase.globalCollectionFactory();
    }

    @Test
    public void testSaveValidateNull() {
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
    public void testSaveEntityNull() {
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
    public void testSaveEntityNotFound() {
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
    public void testSaveEntityNoChange() {
        useService(serviceClass, s -> {
            final ObjectId id = new ObjectId();
            final TestEntity entity = EntityFactory.newInstance(entityClass);
            assertSame(entity, s.save(entity, null));
            final DefaultValidate validate = new DefaultValidate();
            assertNull(s.save(entity, validate));
            assertFalse(validate.hasErrors());
        });
    }

    @Test
    public void testSaveOk() {
        useService(serviceClass, s -> {
            final TestEntity entity = EntityFactory.newInstance(entityClass);
            final DefaultValidate validate = new DefaultValidate();
            assertEquals(entity, s.save(entity, validate));
            assertFalse(validate.hasErrors());
        });
    }

    @Test
    public void testSaveError() {
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
    public void testNoGeneralValidateable() {
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
}
