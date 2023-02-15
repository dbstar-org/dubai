package io.github.dbstarll.dubai.model.collection;

import io.github.dbstarll.dubai.model.MongodTestCase;
import io.github.dbstarll.dubai.model.collection.test.SimpleEntity;
import io.github.dbstarll.dubai.model.collection.test.SimpleEntity.Type;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

class TestCollection extends MongodTestCase {
    @Test
    void testRun() {
        useCollectionFactory(cf -> {
            final Collection<SimpleEntity> collection = cf.newInstance(SimpleEntity.class);
            final SimpleEntity entity = EntityFactory.newInstance(SimpleEntity.class);
            entity.setType(Type.t2);
            entity.setBytes("hello".getBytes(StandardCharsets.UTF_8));
            entity.setSet(true);
            entity.setTypeData(new HashMap<String, java.util.Set<String>>() {{
                put("abc", new HashSet<>(Arrays.asList("aa", "bb")));
            }});

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
