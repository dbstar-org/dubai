package test.io.github.dbstarll.dubai.model.service;

import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.entity.EntityModifier;
import io.github.dbstarll.dubai.model.service.ImplementalAutowirerAware;
import io.github.dbstarll.dubai.model.service.ServiceFactory;
import io.github.dbstarll.dubai.model.service.test3.TestEntity;
import io.github.dbstarll.dubai.model.service.test3.TestService;
import io.github.dbstarll.dubai.model.service.validate.DefaultValidate;
import io.github.dbstarll.dubai.model.service.validate.Validate;
import io.github.dbstarll.dubai.model.service.validate.ValidateException;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestAbstractImplemental {
    @Mocked
    Collection<TestEntity> collection;

    TestService service;

    /**
     * 初始化.
     */
    @Before
    public void setUp() {
        new Expectations() {
            {
                collection.getEntityClass();
                result = TestEntity.class;
            }
        };
        this.service = ServiceFactory.newInstance(TestService.class, collection);
    }

    @After
    public void tearDown() {
        this.service = null;
    }

    @Test
    public void testSaveValidateNull() {
        final TestEntity entity = EntityFactory.newInstance(TestEntity.class);
        entity.setDefunct(true);
        try {
            service.save(entity, null);
            fail("throw ValidateException");
        } catch (Throwable ex) {
            assertEquals(ValidateException.class, ex.getClass());
            final Validate validate = ((ValidateException) ex).getValidate();
            assertNotNull(validate);
            assertTrue(validate.hasErrors());
            assertTrue(validate.hasFieldErrors());
            assertFalse(validate.hasActionErrors());
        }
    }

    @Test
    public void testSaveEntityNull() {
        final DefaultValidate validate = new DefaultValidate();
        assertNull(service.save(null, validate));
        assertTrue(validate.hasErrors());
        assertFalse(validate.hasFieldErrors());
        assertTrue(validate.hasActionErrors());
        assertEquals(Collections.singletonList("实体未设置"), validate.getActionErrors());
    }

    @Test
    public void testSaveEntityNotFound() {
        final ObjectId id = new ObjectId();
        final TestEntity entity = EntityFactory.newInstance(TestEntity.class);
        ((EntityModifier) entity).setId(id);
        final DefaultValidate validate = new DefaultValidate();
        new Expectations() {
            {
                collection.findById(id);
                result = null;
            }
        };
        assertNull(service.save(entity, validate));
        assertTrue(validate.hasErrors());
        assertFalse(validate.hasFieldErrors());
        assertTrue(validate.hasActionErrors());
        assertEquals(Collections.singletonList("实体未找到"), validate.getActionErrors());
        new Verifications() {
            {
                collection.findById(id);
                times = 1;
            }
        };
    }

    @Test
    public void testSaveEntityNoChange() {
        final ObjectId id = new ObjectId();
        final TestEntity entity = EntityFactory.newInstance(TestEntity.class);
        ((EntityModifier) entity).setId(id);
        final DefaultValidate validate = new DefaultValidate();
        new Expectations() {
            {
                collection.findById(id);
                result = entity;
            }
        };
        assertNull(service.save(entity, validate));
        assertFalse(validate.hasErrors());
        new Verifications() {
            {
                collection.findById(id);
                times = 1;
            }
        };
    }

    @Test
    public void testSaveOk() {
        final TestEntity entity = EntityFactory.newInstance(TestEntity.class);
        final DefaultValidate validate = new DefaultValidate();
        new Expectations() {
            {
                collection.save(entity, null);
                result = entity;
            }
        };
        assertEquals(entity, service.save(entity, validate));
        assertFalse(validate.hasErrors());
        new Verifications() {
            {
                collection.save(entity, null);
                times = 1;
            }
        };
    }

    @Test
    public void testSaveError() {
        final TestEntity entity = EntityFactory.newInstance(TestEntity.class);
        final DefaultValidate validate = new DefaultValidate();
        new Expectations() {
            {
                collection.save(entity, null);
                result = new IllegalArgumentException("SaveFailed");
            }
        };
        assertNull(service.save(entity, validate));
        assertTrue(validate.hasActionErrors());
        assertEquals(1, validate.getActionErrors().size());
        assertEquals("[SaveFailed]", validate.getActionErrors().toString());
        new Verifications() {
            {
                collection.save(entity, null);
                times = 1;
            }
        };
    }

    @Test
    public void testNoGeneralValidateable() {
        final InvocationHandler handler = Proxy.getInvocationHandler(service);
        this.service = (TestService) Proxy.newProxyInstance(TestService.class.getClassLoader(),
                new Class[]{TestService.class, ImplementalAutowirerAware.class}, handler);
        final TestEntity entity = EntityFactory.newInstance(TestEntity.class);
        entity.setDefunct(true);
        final DefaultValidate validate = new DefaultValidate();
        assertNotNull(service.save(entity, validate));
        assertFalse(validate.hasErrors());
    }
}
