package io.github.dbstarll.dubai.model.service;

import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.entity.EntityModifier;
import io.github.dbstarll.dubai.model.entity.func.Defunctable;
import io.github.dbstarll.dubai.model.service.test3.TestEntity;
import io.github.dbstarll.dubai.model.service.test3.TestService;
import io.github.dbstarll.dubai.model.service.validate.DefaultValidate;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestDefunctValidation {
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
    public void testInsertSetDefunct() {
        final TestEntity entity = EntityFactory.newInstance(TestEntity.class);
        entity.setDefunct(true);
        final DefaultValidate validate = new DefaultValidate();
        assertNull(service.save(entity, validate));
        assertTrue(validate.hasFieldErrors());
        assertEquals(1, validate.getFieldErrors().size());
        assertEquals(Collections.singletonList("defunct不允许外部修改"),
                validate.getFieldErrors().get(Defunctable.FIELD_NAME_DEFUNCT));
    }

    @Test
    public void testUpdateSetDefunct() {
        final ObjectId id = new ObjectId();
        final TestEntity entity = EntityFactory.newInstance(TestEntity.class);
        ((EntityModifier) entity).setId(id);
        final TestEntity original = EntityFactory.clone(entity);
        entity.setDefunct(true);
        final DefaultValidate validate = new DefaultValidate();

        new Expectations() {
            {
                collection.findById(id);
                result = original;
            }
        };

        assertNull(service.save(entity, validate));
        assertTrue(validate.hasFieldErrors());
        assertEquals(1, validate.getFieldErrors().size());
        assertEquals(Collections.singletonList("defunct不允许外部修改"),
                validate.getFieldErrors().get(Defunctable.FIELD_NAME_DEFUNCT));

        new Verifications() {
            {
                collection.findById(id);
                times = 1;
            }
        };
    }
}
