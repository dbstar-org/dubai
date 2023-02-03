package io.github.dbstarll.dubai.model.collection;

import io.github.dbstarll.dubai.model.MongodTestCase;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.entity.test.NoCloneEntity;
import org.bson.types.ObjectId;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class TestCacheableCollectionCloneNotSupported extends MongodTestCase {
    private final Class<NoCloneEntity> entityClass = NoCloneEntity.class;

    @BeforeClass
    public static void beforeClass() {
        globalCollectionFactory();
    }

    @Test
    public void testFindById() {
        useCollection(entityClass, c -> {
            final NoCloneEntity entity = EntityFactory.newInstance(entityClass);

            final NoCloneEntity saved = c.save(entity);
            assertNotNull(saved);

            assertNull(c.findById(null));

            try {
                c.findById(saved.getId());
                fail("throw CloneNotSupportedException");
            } catch (Throwable ex) {
                assertEquals(UnsupportedOperationException.class, ex.getClass());
                assertEquals(CloneNotSupportedException.class, ex.getCause().getClass());
            }
        });
    }

    @Test
    public void testFindByIdNull() {
        useCollection(entityClass, c -> assertNull(c.findById(new ObjectId())));
    }
}