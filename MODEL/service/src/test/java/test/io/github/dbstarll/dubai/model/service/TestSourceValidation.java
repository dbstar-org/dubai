package test.io.github.dbstarll.dubai.model.service;

import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.entity.info.Sourceable;
import io.github.dbstarll.dubai.model.service.ServiceFactory;
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

import static org.junit.Assert.*;

public class TestSourceValidation {
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
    public void testInsertOk() {
        final TestEntity entity = EntityFactory.newInstance(TestEntity.class);
        entity.setSources(Collections.singletonMap("abc", new ObjectId()));

        new Expectations() {
            {
                collection.save(entity, null);
                result = entity;
            }
        };

        final DefaultValidate validate = new DefaultValidate();
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
    public void testInsertWithEmptyKey() {
        final TestEntity entity = EntityFactory.newInstance(TestEntity.class);
        entity.setSources(Collections.singletonMap("", new ObjectId()));
        final DefaultValidate validate = new DefaultValidate();
        assertNull(service.save(entity, validate));
        assertEquals(Collections.singletonList("来源不能包含空的key或者value"),
                validate.getFieldErrors().get(Sourceable.FIELD_NAME_SOURCES));
    }

    @Test
    public void testInsertWithEmptyValue() {
        final TestEntity entity = EntityFactory.newInstance(TestEntity.class);
        entity.setSources(Collections.singletonMap("key", null));
        final DefaultValidate validate = new DefaultValidate();
        assertNull(service.save(entity, validate));
        assertEquals(Collections.singletonList("来源不能包含空的key或者value"),
                validate.getFieldErrors().get(Sourceable.FIELD_NAME_SOURCES));
    }
}
