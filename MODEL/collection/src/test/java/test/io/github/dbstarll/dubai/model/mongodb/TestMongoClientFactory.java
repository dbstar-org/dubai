package test.io.github.dbstarll.dubai.model.mongodb;

import io.github.dbstarll.dubai.model.mongodb.MongoClientFactory;
import junit.framework.TestCase;

public class TestMongoClientFactory extends TestCase {
    public void testNew() {
        try {
            new MongoClientFactory();
        } catch (Exception ex) {
            fail("throw exception: " + ex);
        }
    }

    /**
     * 测试createWithPojoCodecSplit.
     */
    public void testCreateWithPojoCodecSplit() {
        final MongoClientFactory factory = new MongoClientFactory();
        final String servers = "localhost,localhost:12345";
        final String db = "pumpkin";

        try {
            new MongoClientFactory().createWithPojoCodecSplit(servers, db, ":pumpkin:pumpkin");
            new MongoClientFactory().createWithPojoCodecSplit(servers, db, null);
            new MongoClientFactory().createWithPojoCodecSplit(servers, db, "pumpkin:pumpkin");
            new MongoClientFactory().createWithPojoCodecSplit(servers, db, "pumpkin:pumpkin:pumpkin");
        } catch (Exception ex) {
            fail("throw exception: " + ex);
        }
    }

    public void testCreateWithPojoCodec() {
        final MongoClientFactory factory = new MongoClientFactory();
        try {
            factory.createWithPojoCodec("mongodb://localhost:12345/pumpkin");
        } catch (Exception ex) {
            fail("throw exception: " + ex);
        }
    }
}
