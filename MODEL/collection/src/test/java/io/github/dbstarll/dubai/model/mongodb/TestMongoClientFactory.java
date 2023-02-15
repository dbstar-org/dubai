package io.github.dbstarll.dubai.model.mongodb;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

class TestMongoClientFactory {
    @Test
    void testNew() {
        try {
            new MongoClientFactory();
        } catch (Exception ex) {
            fail("throw exception: " + ex);
        }
    }

    /**
     * 测试createWithPojoCodecSplit.
     */
    @Test
    void testCreateWithPojoCodecSplit() {
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

    @Test
    void testCreateWithPojoCodec() {
        final MongoClientFactory factory = new MongoClientFactory();
        try {
            factory.createWithPojoCodec("mongodb://localhost:12345/pumpkin");
        } catch (Exception ex) {
            fail("throw exception: " + ex);
        }
    }
}
