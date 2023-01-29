package test.io.github.dbstarll.dubai.model.service;

import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.entity.EntityModifier;
import io.github.dbstarll.dubai.model.entity.info.Contentable;
import io.github.dbstarll.dubai.model.service.ServiceFactory;
import io.github.dbstarll.dubai.model.service.test3.contentable.TestContentableEntity;
import io.github.dbstarll.dubai.model.service.test3.contentable.TestContentableService;
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

public class TestContentValidation {
    @Mocked
    Collection<TestContentableEntity> collection;

    TestContentableService service;

    /**
     * 初始化.
     */
    @Before
    public void setUp() {
        new Expectations() {
            {
                collection.getEntityClass();
                result = TestContentableEntity.class;
            }
        };
        this.service = ServiceFactory.newInstance(TestContentableService.class, collection);
    }

    @After
    public void tearDown() {
        this.service = null;
    }

    @Test
    public void testInsertOk() {
        final TestContentableEntity entity = EntityFactory.newInstance(TestContentableEntity.class);
        entity.setContent("content".getBytes());
        entity.setContentType("text/plain");

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
    public void testInsertWithContentNotSet() {
        final TestContentableEntity entity = EntityFactory.newInstance(TestContentableEntity.class);
        entity.setContentType("text/plain");
        final DefaultValidate validate = new DefaultValidate();
        assertNull(service.save(entity, validate));
        assertTrue(validate.hasFieldErrors());
        assertEquals(1, validate.getFieldErrors().size());
        assertEquals(Collections.singletonList("内容未设置"), validate.getFieldErrors().get(Contentable.FIELD_NAME_CONTENT));
    }

    @Test
    public void testInsertWithContentTypeError() {
        new HashMap<String, String>() {{
            put(null, "内容类型未设置");
            put("", "内容类型未设置");
            put("text", "不符合格式：主类型/子类型");
            put("/xml", "缺少主类型");
            put("text/", "缺少子类型");
            put("text/plain/xml", "只能有一个子类型");
        }}.forEach((k, v) -> {
            final TestContentableEntity entity = EntityFactory.newInstance(TestContentableEntity.class);
            entity.setContent("content".getBytes());
            entity.setContentType(k);
            final DefaultValidate valid = new DefaultValidate();
            assertNull(service.save(entity, valid));
            assertTrue(valid.hasFieldErrors());
            assertEquals(1, valid.getFieldErrors().size());
            assertEquals(Collections.singletonList(v), valid.getFieldErrors().get(Contentable.FIELD_NAME_CONTENT_TYPE));
        });
    }

    @Test
    public void testUpdateSetSameContentType() throws CloneNotSupportedException {
        final ObjectId id = new ObjectId();
        final TestContentableEntity entity = EntityFactory.newInstance(TestContentableEntity.class);
        ((EntityModifier) entity).setId(id);
        entity.setContent("content".getBytes());
        entity.setContentType("text/plain");
        final TestContentableEntity original = EntityFactory.clone(entity);
        entity.setContent("new content".getBytes());
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
    public void testUpdateSetDiffContentType() throws CloneNotSupportedException {
        final ObjectId id = new ObjectId();
        final TestContentableEntity entity = EntityFactory.newInstance(TestContentableEntity.class);
        ((EntityModifier) entity).setId(id);
        entity.setContent("content".getBytes());
        entity.setContentType("text/plain");
        final TestContentableEntity original = EntityFactory.clone(entity);
        entity.setContentType("text/xml");
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
}
