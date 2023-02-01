package test.io.github.dbstarll.dubai.model;


import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import de.flapdoodle.embed.mongo.commands.ServerAddress;
import de.flapdoodle.embed.mongo.distribution.Version.Main;
import de.flapdoodle.embed.mongo.transitions.ImmutableMongod.Builder;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.embed.process.io.ProcessOutput;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.transitions.Start;
import io.github.dbstarll.dubai.model.collection.CollectionFactory;
import io.github.dbstarll.utils.lang.bytes.Bytes;
import io.github.dbstarll.utils.lang.digest.Sha256Digestor;
import org.springframework.boot.autoconfigure.mongo.MongoClientFactory;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;

public abstract class MongodTestCase {
    protected static Builder mongodBuilder() {
        return Mongod.builder().processOutput(Start.to(ProcessOutput.class).initializedWith(ProcessOutput.silent()));
    }

    protected static Mongod mongod() {
        return mongodBuilder().build();
    }

    protected Bytes encryptedKey() {
        try {
            return new Bytes(new Sha256Digestor().digest("test".getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }

    protected MongoClientSettings mongoClientSettings() {
        return MongoClientSettings.builder().build();
    }

    protected MongoClientSettingsBuilderCustomizer codecCustomizer(final MongoClientSettings settings) {
        return b -> new io.github.dbstarll.dubai.model.mongodb.MongoClientFactory(encryptedKey())
                .customize(b, settings.getCodecRegistry());
    }

    protected MongoClient mongoClient(final ServerAddress serverAddress) {
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

    protected final void useClient(final Consumer<MongoClient> consumer) {
        try (TransitionWalker.ReachedState<RunningMongodProcess> running = mongod().start(Main.V4_4)) {
            try (MongoClient client = mongoClient(running.current().getServerAddress())) {
                consumer.accept(client);
            }
        }
    }

    protected final void useDatabase(final Consumer<MongoDatabase> consumer) {
        useClient(c -> consumer.accept(c.getDatabase("test")));
    }

    protected final void useCollectionFactory(final Consumer<CollectionFactory> consumer) {
        useDatabase(db -> consumer.accept(new CollectionFactory(db)));
    }
}
