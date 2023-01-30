package test.io.github.dbstarll.dubai.model.spring;


import com.mongodb.client.MongoClient;
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
import io.github.dbstarll.dubai.model.spring.autoconfigure.MongoAutoConfiguration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {MongoAutoConfiguration.class}, webEnvironment = WebEnvironment.NONE)
public class TestMongoAutoConfiguration implements ApplicationContextAware {
    private static ReachedState<RunningMongodProcess> state;
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @BeforeClass
    public static void setup() {
        System.out.println("setup");
        state = Mongod.builder()
                .net(Start.to(Net.class).initializedWith(Net.of("localhost", 27017, false)))
                .processOutput(Derive.given(Name.class).state(ProcessOutput.class).deriveBy(name -> ProcessOutput.silent()))
                .build()
                .start(Main.V4_4);
    }

    @AfterClass
    public static void clean() {
        System.out.println("clean");
        if (state != null) {
            state.close();
            state = null;
        }
    }

    @Test
    public void testGetMongoDatabase() {
        assertNotNull(applicationContext.getBean("mongoDatabase", MongoDatabase.class));
    }

    @Test
    public void testGetMongoClient() {
        assertNotNull(applicationContext.getBean("mongoClient", MongoClient.class));
    }
}
