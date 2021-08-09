package test.io.github.dbstarll.dubai.model.mongodb;

import io.github.dbstarll.dubai.model.mongodb.MongoClientFactory;
import junit.framework.TestCase;

import java.security.NoSuchAlgorithmException;

public class TestMongoClientFactory extends TestCase {
    public void testNew() throws NoSuchAlgorithmException {
        new MongoClientFactory();
    }

    /**
     * 测试createWithPojoCodecSplit.
     *
     * @throws NoSuchAlgorithmException 无此加密算法
     */
    public void testCreateWithPojoCodecSplit() throws NoSuchAlgorithmException {
        new MongoClientFactory().createWithPojoCodecSplit("localhost,localhost:12345", "pumpkin", ":pumpkin:pumpkin");
        new MongoClientFactory().createWithPojoCodecSplit("localhost,localhost:12345", "pumpkin", null);
        new MongoClientFactory().createWithPojoCodecSplit("localhost,localhost:12345", "pumpkin", "pumpkin:pumpkin");
        new MongoClientFactory().createWithPojoCodecSplit("localhost,localhost:12345", "pumpkin",
                "pumpkin:pumpkin:pumpkin");
    }

    public void testCreateWithPojoCodec() throws NoSuchAlgorithmException {
        new MongoClientFactory().createWithPojoCodec("mongodb://localhost:12345/pumpkin");
    }
}
