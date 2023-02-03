package io.github.dbstarll.dubai.model.service;

import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.entity.EntityModifier;
import io.github.dbstarll.dubai.model.entity.info.Namable;
import io.github.dbstarll.dubai.model.service.test3.namable.TestNamableEntity;
import io.github.dbstarll.dubai.model.service.test3.namable.TestNamableService;
import io.github.dbstarll.dubai.model.service.validate.DefaultValidate;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestNameValidation {
    @Mocked
    Collection<TestNamableEntity> collection;

    TestNamableService service;

    /**
     * 初始化.
     */
    @Before
    public void setUp() {
        new Expectations() {
            {
                collection.getEntityClass();
                result = TestNamableEntity.class;
            }
        };
        this.service = ServiceFactory.newInstance(TestNamableService.class, collection);
    }

    @After
    public void tearDown() {
        this.service = null;
    }

    @Test
    public void testInsertSetName() {
        final TestNamableEntity entity = EntityFactory.newInstance(TestNamableEntity.class);
        entity.setName("name");
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
    public void testInsertSetNameError() {
        new HashMap<String, String>() {{
            put(null, "名称未设置");
            put("", "名称未设置");
            put("\tname", "名称不能以空字符开头");
            put("name\t", "名称不能以空字符结尾");
            put("na\t\tme  jone", "名称不能包含连续的空字符");
            put("n", "名称不能少于 2 字符");
            put("01234567890123456789", "名称不能超过 16 字符");
        }}.forEach((k, v) -> {
            final TestNamableEntity entity = EntityFactory.newInstance(TestNamableEntity.class);
            entity.setName(k);
            final DefaultValidate validate = new DefaultValidate();
            assertNull(service.save(entity, validate));
            assertTrue(validate.hasFieldErrors());
            assertEquals(1, validate.getFieldErrors().size());
            assertEquals(Collections.singletonList(v), validate.getFieldErrors().get(Namable.FIELD_NAME_NAME));
        });
    }

    @Test
    public void testUpdateSetName() {
        final ObjectId id = new ObjectId();
        final TestNamableEntity entity = EntityFactory.newInstance(TestNamableEntity.class);
        ((EntityModifier) entity).setId(id);
        entity.setName("name");
        final TestNamableEntity original = EntityFactory.clone(entity);
        entity.setName("name2");
        final DefaultValidate validate = new DefaultValidate();

        new Expectations() {
            {
                collection.findById(id);
                result = original;
                collection.save(entity, null);
                result = entity;
            }
        };

        assertEquals(entity, service.save(entity, validate));
        assertFalse(validate.hasErrors());

        new Verifications() {
            {
                collection.findById(id);
                times = 1;
                collection.save(entity, null);
                times = 1;
            }
        };
    }

    @Test
    public void testUpdateSetNameSame() {
        final ObjectId id = new ObjectId();
        final TestNamableEntity entity = EntityFactory.newInstance(TestNamableEntity.class);
        ((EntityModifier) entity).setId(id);
        entity.setName("name");
        final TestNamableEntity original = EntityFactory.clone(entity);
        final DefaultValidate validate = new DefaultValidate();

        new Expectations() {
            {
                collection.findById(id);
                result = original;
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
    public void testNameValidation() {
        try {
            service.name(5, 4);
            fail("throw IllegalArgumentException");
        } catch (Throwable ex) {
            assertEquals(IllegalArgumentException.class, ex.getClass());
            assertEquals("maxLength: 4 必须>= minLength: 5", ex.getMessage());
        }
    }
}
