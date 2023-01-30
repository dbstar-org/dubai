package test.io.github.dbstarll.dubai.model.service;

import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.entity.EntityModifier;
import io.github.dbstarll.dubai.model.entity.info.Describable;
import io.github.dbstarll.dubai.model.service.ServiceFactory;
import io.github.dbstarll.dubai.model.service.test3.TestEntity;
import io.github.dbstarll.dubai.model.service.test3.TestService;
import io.github.dbstarll.dubai.model.service.validate.DefaultValidate;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestDescriptionValidation {
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
    public void testInsertSetDescription() {
        final TestEntity entity = EntityFactory.newInstance(TestEntity.class);
        entity.setDescription("description");
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
    public void testInsertSetDescriptionEmpty() {
        final TestEntity entity = EntityFactory.newInstance(TestEntity.class);
        entity.setDescription("    ");
        final DefaultValidate validate = new DefaultValidate();
        new Expectations() {
            {
                collection.save(entity, null);
                result = entity;
            }
        };
        assertEquals(entity, service.save(entity, validate));
        assertFalse(validate.hasErrors());
        assertNull(entity.getDescription());
        new Verifications() {
            {
                collection.save(entity, null);
                times = 1;
            }
        };
    }

    @Test
    public void testInsertSetDescriptionLong() {
        final TestEntity entity = EntityFactory.newInstance(TestEntity.class);
        entity.setDescription(StringUtils.repeat("0123456789", 6));
        final DefaultValidate validate = new DefaultValidate();
        assertNull(service.save(entity, validate));
        assertTrue(validate.hasFieldErrors());
        assertEquals(1, validate.getFieldErrors().size());
        assertEquals(Collections.singletonList("备注不能超过 50 字符"),
                validate.getFieldErrors().get(Describable.FIELD_NAME_DESCRIPTION));
    }

    @Test
    public void testUpdateSetDescription() {
        final ObjectId id = new ObjectId();
        final TestEntity entity = EntityFactory.newInstance(TestEntity.class);
        ((EntityModifier) entity).setId(id);
        final TestEntity original = EntityFactory.clone(entity);
        entity.setDescription("description");
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
    public void testUpdateSetDescriptionSame() {
        final ObjectId id = new ObjectId();
        final TestEntity entity = EntityFactory.newInstance(TestEntity.class);
        ((EntityModifier) entity).setId(id);
        entity.setDescription("description");
        final TestEntity original = EntityFactory.clone(entity);
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
}
