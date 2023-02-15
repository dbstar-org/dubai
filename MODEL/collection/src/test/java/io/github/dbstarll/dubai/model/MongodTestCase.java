package io.github.dbstarll.dubai.model;


import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import de.flapdoodle.embed.mongo.commands.ServerAddress;
import de.flapdoodle.embed.mongo.distribution.Version.Main;
import de.flapdoodle.embed.mongo.transitions.ImmutableMongod.Builder;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.embed.process.io.ProcessOutput;
import de.flapdoodle.reverse.TransitionWalker.ReachedState;
import de.flapdoodle.reverse.transitions.Start;
import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.collection.CollectionFactory;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.utils.lang.bytes.Bytes;
import io.github.dbstarll.utils.lang.digest.Sha256Digestor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.springframework.boot.autoconfigure.mongo.MongoClientFactory;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;

public abstract class MongodTestCase {
    private static ReachedState<RunningMongodProcess> globalRunning;
    private static MongoClient globalClient;
    private static MongoDatabase globalDatabase;
    private static CollectionFactory globalFactory;

    protected static void globalMongod() {
        if (globalRunning == null) {
            globalRunning = mongod().start(Main.V4_4);
        }
    }

    protected static void globalMongoClient() {
        globalMongod();
        if (globalClient == null) {
            globalClient = mongoClient(globalRunning.current().getServerAddress());
        }
    }

    protected static void globalMongoDatabase() {
        globalMongoClient();
        if (globalDatabase == null) {
            globalDatabase = globalClient.getDatabase("test");
        }
    }

    protected static void globalCollectionFactory() {
        globalMongoDatabase();
        if (globalFactory == null) {
            globalFactory = new CollectionFactory(globalDatabase);
        }
    }

    @AfterAll
    public static void cleanupGlobal() {
        if (globalFactory != null) {
            globalFactory = null;
        }
        if (globalDatabase != null) {
            globalDatabase.drop();
            globalDatabase = null;
        }
        if (globalClient != null) {
            globalClient.close();
            globalClient = null;
        }
        if (globalRunning != null) {
            globalRunning.close();
            globalRunning = null;
        }
    }

    @AfterEach
    public void cleanupTest() {
        if (globalDatabase != null) {
            globalDatabase.drop();
        }
    }

    protected static Builder mongodBuilder() {
        return Mongod.builder().processOutput(Start.to(ProcessOutput.class).initializedWith(ProcessOutput.silent()));
    }

    protected static Mongod mongod() {
        return mongodBuilder().build();
    }

    protected static Bytes encryptedKey() {
        try {
            return new Bytes(new Sha256Digestor().digest("test".getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }

    protected static MongoClientSettings mongoClientSettings() {
        return MongoClientSettings.builder().build();
    }

    protected static MongoClientSettingsBuilderCustomizer codecCustomizer(final MongoClientSettings settings) {
        return b -> new io.github.dbstarll.dubai.model.mongodb.MongoClientFactory(encryptedKey())
                .customize(b, settings.getCodecRegistry());
    }

    protected static MongoClient mongoClient(final ServerAddress serverAddress) {
        final MongoClientSettings settings = mongoClientSettings();
        final MongoClientSettingsBuilderCustomizer serverCustomizer = b -> b.applyToClusterSettings(
                c -> c.hosts(Collections.singletonList(
                        new com.mongodb.ServerAddress(serverAddress.getHost(), serverAddress.getPort())
                ))
        );
        return new MongoClientFactory(Arrays.asList(
                codecCustomizer(settings),
                serverCustomizer
        )).createMongoClient(settings);
    }

    protected final void useMongod(final Consumer<ReachedState<RunningMongodProcess>> consumer) {
        if (globalRunning == null) {
            try (ReachedState<RunningMongodProcess> running = mongod().start(Main.V4_4)) {
                consumer.accept(running);
            }
        } else {
            consumer.accept(globalRunning);
        }
    }

    protected final void useClient(final Consumer<MongoClient> consumer) {
        if (globalClient == null) {
            useMongod(d -> {
                try (MongoClient client = mongoClient(d.current().getServerAddress())) {
                    consumer.accept(client);
                }
            });
        } else {
            consumer.accept(globalClient);
        }
    }

    protected final void useDatabase(final Consumer<MongoDatabase> consumer) {
        if (globalDatabase == null) {
            useClient(c -> consumer.accept(c.getDatabase("test")));
        } else {
            consumer.accept(globalDatabase);
        }
    }

    protected final void useCollectionFactory(final Consumer<CollectionFactory> consumer) {
        if (globalFactory == null) {
            useDatabase(db -> consumer.accept(new CollectionFactory(db)));
        } else {
            consumer.accept(globalFactory);
        }
    }

    protected final <E extends Entity> void useCollection(final Class<E> entityClass,
                                                          final Consumer<Collection<E>> consumer) {
        useCollectionFactory(cf -> consumer.accept(cf.newInstance(entityClass)));
    }
}
