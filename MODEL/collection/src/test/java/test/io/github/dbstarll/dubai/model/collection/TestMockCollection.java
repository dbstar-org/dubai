package test.io.github.dbstarll.dubai.model.collection;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoDriverInformation;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.internal.MongoClientImpl;
import com.mongodb.client.internal.OperationExecutor;
import com.mongodb.connection.SocketSettings;
import com.mongodb.connection.SocketStreamFactory;
import com.mongodb.connection.StreamFactory;
import com.mongodb.connection.StreamFactoryFactory;
import com.mongodb.internal.bulk.InsertRequest;
import com.mongodb.internal.connection.Cluster;
import com.mongodb.internal.connection.DefaultClusterFactory;
import com.mongodb.internal.connection.InternalConnectionPoolSettings;
import com.mongodb.internal.event.EventListenerHelper;
import com.mongodb.internal.operation.MixedBulkWriteOperation;
import com.mongodb.internal.operation.ReadOperation;
import com.mongodb.internal.operation.WriteOperation;
import com.mongodb.lang.Nullable;
import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.collection.CollectionFactory;
import io.github.dbstarll.dubai.model.collection.test.SimpleEntity;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.mongodb.MongoClientFactory;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class TestMockCollection {
    private MongoDriverInformation mongoDriverInformation;
    private MongoClientSettings settings;
    private Cluster cluster;

    @Injectable
    private OperationExecutor operationExecutor;

    @Mocked
    private BulkWriteResult bulkWriteResult;

    @Before
    public void initialize() {
        mongoDriverInformation = MongoDriverInformation.builder().driverName("sync").build();
        settings = new MongoClientFactory().getMongoClientSettingsbuilder().applyToClusterSettings(
                s -> s.serverSelectionTimeout(100, TimeUnit.MILLISECONDS)
        ).build();
        cluster = createCluster(settings, mongoDriverInformation);
    }

    private static Cluster createCluster(MongoClientSettings settings, @Nullable MongoDriverInformation mongoDriverInformation) {
        return new DefaultClusterFactory().createCluster(
                settings.getClusterSettings(),
                settings.getServerSettings(),
                settings.getConnectionPoolSettings(),
                InternalConnectionPoolSettings.builder().build(),
                getStreamFactory(settings, false),
                getStreamFactory(settings, true),
                settings.getCredential(),
                EventListenerHelper.getCommandListener(settings.getCommandListeners()),
                settings.getApplicationName(),
                mongoDriverInformation,
                settings.getCompressorList(),
                settings.getServerApi());
    }

    private static StreamFactory getStreamFactory(MongoClientSettings settings, boolean isHeartbeat) {
        StreamFactoryFactory streamFactoryFactory = settings.getStreamFactoryFactory();
        SocketSettings socketSettings = isHeartbeat ? settings.getHeartbeatSocketSettings() : settings.getSocketSettings();
        return streamFactoryFactory == null ? new SocketStreamFactory(socketSettings, settings.getSslSettings()) : streamFactoryFactory.create(socketSettings, settings.getSslSettings());
    }

    @Test
    public void testMock() {
        try (final MongoClient client = new MongoClientImpl(cluster, mongoDriverInformation, settings, new OperationExecutor() {
            @Override
            public <T> T execute(ReadOperation<T> readOperation, ReadPreference readPreference, ReadConcern readConcern) {
                return null;
            }

            @Override
            public <T> T execute(WriteOperation<T> writeOperation, ReadConcern readConcern) {
                return null;
            }

            @Override
            public <T> T execute(ReadOperation<T> readOperation, ReadPreference readPreference, ReadConcern readConcern, ClientSession clientSession) {
                return null;
            }

            @Override
            public <T> T execute(WriteOperation<T> writeOperation, ReadConcern readConcern, ClientSession clientSession) {
                System.out.println(writeOperation);
                final MixedBulkWriteOperation op = (MixedBulkWriteOperation) writeOperation;
                op.getWriteRequests().forEach(a -> {
                    final InsertRequest r = (InsertRequest) a;
                    System.out.println(r.getDocument());
                });
                return (T) bulkWriteResult;
            }
        })) {
            final MongoDatabase db = client.getDatabase("test");
            final CollectionFactory cf = new CollectionFactory(db);
            final Collection<SimpleEntity> collection = cf.newInstance(SimpleEntity.class);
            final SimpleEntity entity = EntityFactory.newInstance(SimpleEntity.class);

            new Expectations() {
                {
//                    operationExecutor.execute((WriteOperation<?>) any, (ReadConcern) any, (ClientSession) any);
//                    result = bulkWriteResult;
//                    bulkWriteResult.getInserts();
//                    result = 1;
                }
            };

            System.out.println(collection.save(entity));
        }
    }
}
