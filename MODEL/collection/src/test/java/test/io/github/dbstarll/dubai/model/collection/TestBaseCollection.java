package test.io.github.dbstarll.dubai.model.collection;

import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.github.dbstarll.dubai.model.collection.BaseCollection;
import io.github.dbstarll.dubai.model.collection.test.SimpleEntity;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Verifications;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class TestBaseCollection {
    @Injectable
    MongoDatabase mongoDatabase;

    @Mocked
    MongoCollection<SimpleEntity> mongoCollection;

    @Mocked
    MongoNamespace mongoNamespace;

    private BaseCollection<SimpleEntity> collection;

    /**
     * 初始化collection.
     */
    @Before
    public void initialize() {
        this.collection = new BaseCollection<>(mongoCollection);
    }

    @Test
    public void testGetNamespace() {
        new Expectations() {
            {
                mongoCollection.getNamespace();
                result = mongoNamespace;
                mongoNamespace.getFullName();
                result = "fullNamespace";
            }
        };
        assertEquals("fullNamespace", collection.getNamespace());
        new Verifications() {
            {
                mongoCollection.getNamespace();
                times = 1;
                mongoNamespace.getFullName();
                times = 1;
            }
        };
    }

    @Test
    public void testGetMongoCollection() {
        assertSame(mongoCollection, collection.getMongoCollection());
    }
}
