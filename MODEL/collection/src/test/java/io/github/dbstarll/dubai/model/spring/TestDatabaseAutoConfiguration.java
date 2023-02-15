package io.github.dbstarll.dubai.model.spring;


import com.mongodb.client.MongoDatabase;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version.Main;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.reverse.TransitionWalker.ReachedState;
import de.flapdoodle.reverse.transitions.Start;
import io.github.dbstarll.dubai.model.MongodTestCase;
import io.github.dbstarll.dubai.model.mongodb.codecs.EncryptedByteArrayCodec;
import io.github.dbstarll.dubai.model.spring.autoconfigure.DatabaseAutoConfiguration;
import org.bson.codecs.configuration.CodecRegistry;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@SpringBootTest(webEnvironment = WebEnvironment.NONE,
        classes = {
                MongoAutoConfiguration.class,
                DatabaseAutoConfiguration.class,
        },
        properties = {
                "dubai.model.collection.encryptedKey=test",
                "spring.data.mongodb.host=127.0.0.1",
                "spring.data.mongodb.username=username",
                "spring.data.mongodb.password=password",
                "spring.data.mongodb.authenticationDatabase=pumpkin"
        })
class TestDatabaseAutoConfiguration extends MongodTestCase {
    private static ReachedState<RunningMongodProcess> state;

    @BeforeAll
    static void setup() {
        state = mongodBuilder()
                .net(Start.to(Net.class).initializedWith(Net.of("localhost", 27017, false)))
                .build().start(Main.V4_4);
    }

    @AfterAll
    static void clean() {
        if (state != null) {
            state.close();
            state = null;
        }
    }

    @Autowired
    private MongoDatabase db;

    @Test
    void testMongoDatabase() {
        assertEquals("test", db.getName());
    }

    @Test
    void testCodecRegistry() {
        final CodecRegistry registry = db.getCodecRegistry();
        assertSame(EncryptedByteArrayCodec.class, registry.get(byte[].class).getClass());
    }
}
