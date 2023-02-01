package test.io.github.dbstarll.dubai.model.collection;

import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.collection.test.SimpleEntity;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import org.junit.Test;
import test.io.github.dbstarll.dubai.model.MongodTestCase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

public class TestCollection extends MongodTestCase {
    @Test
    public void testRun() {
        useCollectionFactory(cf -> {
            final Collection<SimpleEntity> collection = cf.newInstance(SimpleEntity.class);
            final SimpleEntity entity = EntityFactory.newInstance(SimpleEntity.class);

            final SimpleEntity saved = collection.save(entity);
            assertNotNull(saved);
            assertSame(entity, saved);

            assertEquals(1, collection.count());

            final SimpleEntity loaded = collection.findById(entity.getId());
            assertNotNull(loaded);
            assertEquals(saved, loaded);
            assertNotSame(saved, loaded);
        });
    }
}
