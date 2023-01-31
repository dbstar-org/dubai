package test.io.github.dbstarll.dubai.model.spring;


import com.mongodb.client.MongoDatabase;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version.Main;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.embed.process.io.ProcessOutput;
import de.flapdoodle.embed.process.types.Name;
import de.flapdoodle.reverse.TransitionWalker.ReachedState;
import de.flapdoodle.reverse.transitions.Derive;
import de.flapdoodle.reverse.transitions.Start;
import io.github.dbstarll.dubai.model.mongodb.codecs.EncryptedByteArrayCodec;
import io.github.dbstarll.dubai.model.spring.autoconfigure.DatabaseAutoConfiguration;
import org.bson.codecs.configuration.CodecRegistry;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.NONE,
        classes = {
                MongoAutoConfiguration.class,
                DatabaseAutoConfiguration.class,
        },
        properties = {
                "dubai.mongodb.encryptedKey=test",
                "spring.data.mongodb.host=127.0.0.1",
                "spring.data.mongodb.username=username",
                "spring.data.mongodb.password=password",
                "spring.data.mongodb.authenticationDatabase=pumpkin"
        })
public class TestDatabaseAutoConfiguration {
    private static ReachedState<RunningMongodProcess> state;

    @BeforeClass
    public static void setup() {
        state = Mongod.builder()
                .net(Start.to(Net.class).initializedWith(Net.of("localhost", 27017, false)))
                .processOutput(Derive.given(Name.class).state(ProcessOutput.class).deriveBy(name -> ProcessOutput.silent()))
                .build()
                .start(Main.V4_4);
    }

    @AfterClass
    public static void clean() {
        if (state != null) {
            state.close();
            state = null;
        }
    }

    @Autowired
    private MongoDatabase db;

    @Test
    public void testMongoDatabase() {
        assertEquals("test", db.getName());
    }

    @Test
    public void testCodecRegistry() {
        final CodecRegistry registry = db.getCodecRegistry();
        assertSame(EncryptedByteArrayCodec.class, registry.get(byte[].class).getClass());
    }
}
