package test.io.github.dbstarll.dubai.model.mongodb;

import io.github.dbstarll.dubai.model.mongodb.MongoClientFactory;
import junit.framework.TestCase;

public class TestMongoClientFactory extends TestCase {
    public void testNew() {
        new MongoClientFactory();
    }

    /**
     * 测试createWithPojoCodecSplit.
     */
    public void testCreateWithPojoCodecSplit() {
        new MongoClientFactory().createWithPojoCodecSplit("localhost,localhost:12345", "pumpkin", ":pumpkin:pumpkin");
        new MongoClientFactory().createWithPojoCodecSplit("localhost,localhost:12345", "pumpkin", null);
        new MongoClientFactory().createWithPojoCodecSplit("localhost,localhost:12345", "pumpkin", "pumpkin:pumpkin");
        new MongoClientFactory().createWithPojoCodecSplit("localhost,localhost:12345", "pumpkin",
                "pumpkin:pumpkin:pumpkin");
    }

    public void testCreateWithPojoCodec() {
        new MongoClientFactory().createWithPojoCodec("mongodb://localhost:12345/pumpkin");
    }
}
