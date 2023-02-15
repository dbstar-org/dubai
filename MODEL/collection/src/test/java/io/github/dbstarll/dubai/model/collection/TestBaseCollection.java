package io.github.dbstarll.dubai.model.collection;

import com.mongodb.client.MongoCollection;
import io.github.dbstarll.dubai.model.MongodTestCase;
import io.github.dbstarll.dubai.model.collection.test.SimpleEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

public class TestBaseCollection extends MongodTestCase {
    @BeforeAll
    public static void beforeClass() {
        globalMongoDatabase();
    }

    @Test
    public void testGetMongoCollection() {
        useDatabase(db -> {
            final MongoCollection<SimpleEntity> mongoCollection = db.getCollection("simpleEntity", SimpleEntity.class);
            final BaseCollection<SimpleEntity> collection = new BaseCollection<>(mongoCollection);
            assertSame(mongoCollection, collection.getMongoCollection());
        });
    }

    @Test
    public void testGetEntityClass() {
        useDatabase(db -> {
            final MongoCollection<SimpleEntity> mongoCollection = db.getCollection("simpleEntity", SimpleEntity.class);
            final BaseCollection<SimpleEntity> collection = new BaseCollection<>(mongoCollection);
            assertSame(SimpleEntity.class, collection.getEntityClass());
        });
    }
}
