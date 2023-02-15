package io.github.dbstarll.dubai.model.collection;

import io.github.dbstarll.dubai.model.MongodTestCase;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.entity.test.NoCloneEntity;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

class TestCacheableCollectionCloneNotSupported extends MongodTestCase {
    private final Class<NoCloneEntity> entityClass = NoCloneEntity.class;

    @BeforeAll
    static void beforeClass() {
        globalCollectionFactory();
    }

    @Test
    void testFindById() {
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
    void testFindByIdNull() {
        useCollection(entityClass, c -> assertNull(c.findById(new ObjectId())));
    }
}
