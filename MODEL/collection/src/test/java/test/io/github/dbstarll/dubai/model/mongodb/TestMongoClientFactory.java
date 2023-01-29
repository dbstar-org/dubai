package test.io.github.dbstarll.dubai.model.mongodb;

import io.github.dbstarll.dubai.model.mongodb.MongoClientFactory;
import junit.framework.TestCase;

public class TestMongoClientFactory extends TestCase {
    public void testNew() {
        assertNotNull(new MongoClientFactory());
    }

    /**
     * 测试createWithPojoCodecSplit.
     */
    public void testCreateWithPojoCodecSplit() {
        assertNotNull(new MongoClientFactory().createWithPojoCodecSplit("localhost,localhost:12345", "pumpkin", ":pumpkin:pumpkin"));
        assertNotNull(new MongoClientFactory().createWithPojoCodecSplit("localhost,localhost:12345", "pumpkin", null));
        assertNotNull(new MongoClientFactory().createWithPojoCodecSplit("localhost,localhost:12345", "pumpkin", "pumpkin:pumpkin"));
        assertNotNull(new MongoClientFactory().createWithPojoCodecSplit("localhost,localhost:12345", "pumpkin",
                "pumpkin:pumpkin:pumpkin"));
    }

    public void testCreateWithPojoCodec() {
        assertNotNull(new MongoClientFactory().createWithPojoCodec("mongodb://localhost:12345/pumpkin"));
    }
}
