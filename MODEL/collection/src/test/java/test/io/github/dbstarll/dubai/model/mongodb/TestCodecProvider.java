package test.io.github.dbstarll.dubai.model.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoTimeoutException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.internal.MongoClientImpl;
import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.collection.CollectionFactory;
import io.github.dbstarll.dubai.model.collection.test.DiffGetterSetterEntity;
import io.github.dbstarll.dubai.model.collection.test.DirectMethodGenericEntity;
import io.github.dbstarll.dubai.model.collection.test.InheritMethodGenericEntity;
import io.github.dbstarll.dubai.model.collection.test.InheritMethodGenericHidingEntity;
import io.github.dbstarll.dubai.model.collection.test.MultiSetterEntity;
import io.github.dbstarll.dubai.model.collection.test.NotEntity;
import io.github.dbstarll.dubai.model.collection.test.OnlyGetterEntity;
import io.github.dbstarll.dubai.model.collection.test.OnlySetterEntity;
import io.github.dbstarll.dubai.model.collection.test.SimpleClassEntity;
import io.github.dbstarll.dubai.model.collection.test.SimpleEntity;
import io.github.dbstarll.dubai.model.collection.test.SimpleEntity.Type;
import io.github.dbstarll.dubai.model.collection.test.SimpleGenericEntity;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.entity.EntityModifier;
import io.github.dbstarll.dubai.model.mongodb.MongoClientFactory;
import io.github.dbstarll.dubai.model.mongodb.codecs.EncryptedByteArrayCodec;
import io.github.dbstarll.utils.lang.EncryptUtils;
import io.github.dbstarll.utils.lang.bytes.Bytes;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.bson.BsonBinary;
import org.bson.BsonDocument;
import org.bson.BsonDocumentReader;
import org.bson.BsonDocumentWriter;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecConfigurationException;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.ObjectId;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertArrayEquals;

public class TestCodecProvider extends TestCase {
    private MongoClient client;
    private MongoDatabase database;
    private CollectionFactory collectionFactory;

    @Override
    protected void setUp() throws Exception {
        this.client = MongoClients.create(
                new MongoClientFactory(new Bytes(EncryptUtils.sha("y1cloud.com", 256))).getMongoClientSettingsBuilder()
                        .applyConnectionString(new ConnectionString("mongodb://localhost:12345/pumpkin"))
                        .applyToClusterSettings(s -> s.serverSelectionTimeout(100, TimeUnit.MILLISECONDS)).build()
        );
        this.database = client.getDatabase("test");
        this.collectionFactory = new CollectionFactory(database);
    }

    @Override
    protected void tearDown() {
        this.collectionFactory = null;
        this.database = null;
        this.client.close();
        this.client = null;
    }

    /**
     * 测试基于接口的Entity.
     */
    public void testInterfaceEntity() {
        final Collection<SimpleEntity> collection = collectionFactory.newInstance(SimpleEntity.class);
        final SimpleEntity entity = EntityFactory.newInstance(SimpleEntity.class);
        entity.setType(Type.t1);
        entity.setBytes(new ObjectId().toByteArray());

        try {
            collection.save(entity);
            fail("throw MongoTimeoutException");
        } catch (Throwable ex) {
            assertEquals(MongoTimeoutException.class, ex.getClass());
        }
    }

    /**
     * 测试基于Class的Entity.
     */
    public void testClassEntity() {
        final Collection<SimpleClassEntity> collection = collectionFactory.newInstance(SimpleClassEntity.class);
        final SimpleClassEntity entity = EntityFactory.newInstance(SimpleClassEntity.class);

        try {
            collection.save(entity);
            fail("throw MongoTimeoutException");
        } catch (Throwable ex) {
            assertEquals(MongoTimeoutException.class, ex.getClass());
        }
    }

    /**
     * 测试未实现PojoFields的Proxy.
     */
    public void testProxyNoPojoFields() {
        final Collection<SimpleEntity> collection = collectionFactory.newInstance(SimpleEntity.class);
        final SimpleEntity entity = (SimpleEntity) Proxy.newProxyInstance(SimpleEntity.class.getClassLoader(),
                new Class[]{SimpleEntity.class, EntityModifier.class}, (proxy, method, args) -> null);

        try {
            collection.save(entity);
            fail("throw MongoTimeoutException");
        } catch (Throwable ex) {
            assertEquals(MongoTimeoutException.class, ex.getClass());
        }
    }

    /**
     * 测试保存一个非Entity的对象.
     */
    public void testNoEntity() {
        final MongoCollection<NotEntity> collection = database.getCollection("oid", NotEntity.class);

        try {
            collection.insertOne(new NotEntity());
            fail("throw CodecConfigurationException");
        } catch (Throwable ex) {
            assertCodecNotFound(ex);
        }
    }

    /**
     * 测试ImageCodec.
     */
    public void testImageCodecEncode() throws IOException {
        final CodecRegistry registry = ((MongoClientImpl) client).getCodecRegistry();
        final Codec<byte[]> codec = registry.get(byte[].class);
        assertSame(EncryptedByteArrayCodec.class, codec.getClass());
        assertSame(byte[].class, codec.getEncoderClass());

        testImageCodec(codec, "png.png", true, true);
        testImageCodec(codec, "jpg.jpg", true, true);
        testImageCodec(codec, "ico.ico", false, true);
        testImageCodec(codec, "txt.txt", false, true);
    }

    /**
     * 测试ImageCodec.
     */
    public void testImageCodecNotEncode() throws IOException {
        this.client = MongoClients.create(
                new MongoClientFactory().getMongoClientSettingsBuilder()
                        .applyConnectionString(new ConnectionString("mongodb://localhost:12345/pumpkin"))
                        .applyToClusterSettings(s -> s.serverSelectionTimeout(100, TimeUnit.MILLISECONDS)).build()
        );
        final CodecRegistry registry = ((MongoClientImpl) client).getCodecRegistry();
        final Codec<byte[]> codec = registry.get(byte[].class);
        assertSame(EncryptedByteArrayCodec.class, codec.getClass());
        assertSame(byte[].class, codec.getEncoderClass());

        testImageCodec(codec, "png.png", true, false);
        testImageCodec(codec, "jpg.jpg", true, false);
        testImageCodec(codec, "ico.ico", false, false);
        testImageCodec(codec, "txt.txt", false, false);
    }

    private void testImageCodec(final Codec<byte[]> codec, String resource, boolean image, boolean encode)
            throws IOException {
        final BsonDocument document = new BsonDocument();
        final BsonWriter writer = new BsonDocumentWriter(document);
        final byte[] data = read(resource);
        writer.writeStartDocument();
        writer.writeName("data");
        codec.encode(writer, data, EncoderContext.builder().build());
        final byte[] save = document.getBinary("data").getData();
        assertEquals(data.length, save.length);
        assertEquals(!encode, Arrays.equals(data, save));

        document.append("original", new BsonBinary(data));

        final BsonReader reader = new BsonDocumentReader(document);
        reader.readStartDocument();
        reader.readName();
        final byte[] load = codec.decode(reader, DecoderContext.builder().build());
        assertEquals(!encode, Arrays.equals(load, save));
        assertArrayEquals(load, data);

        reader.readName();
        final byte[] original = codec.decode(reader, DecoderContext.builder().build());
        if (image) {
            assertEquals(!encode, Arrays.equals(original, save));
            assertArrayEquals(original, data);
        } else {
            assertArrayEquals(original, save);
            assertEquals(!encode, Arrays.equals(original, data));
        }
    }

    private byte[] read(String resource) throws IOException {
        try (InputStream in = ClassLoader.getSystemResourceAsStream(resource)) {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                try {
                    IOUtils.copy(in, out);
                } finally {
                    out.close();
                }
                return out.toByteArray();
            }
        }
    }

    /**
     * 测试基于范型接口的Entity.
     */
    public void testGenericEntity() {
        final Collection<SimpleGenericEntity> collection = collectionFactory.newInstance(SimpleGenericEntity.class);
        final SimpleGenericEntity entity = EntityFactory.newInstance(SimpleGenericEntity.class);
        entity.setKey("key");
        entity.setValue(100);

        try {
            collection.save(entity);
            fail("throw MongoTimeoutException");
        } catch (Throwable ex) {
            assertEquals(MongoTimeoutException.class, ex.getClass());
        }
    }

    /**
     * 测试基于多个同名属性的Entity.
     */
    public void testMultiSetterEntity() {
        final Collection<MultiSetterEntity> collection = collectionFactory.newInstance(MultiSetterEntity.class);
        final MultiSetterEntity entity = EntityFactory.newInstance(MultiSetterEntity.class);
        entity.setData(true);
        entity.setData(100);

        try {
            collection.save(entity);
            fail("throw CodecConfigurationException");
        } catch (Throwable ex) {
            assertCodecNotFound(ex);
        }
    }

    /**
     * 测试属性的getter和setter类型不一致的Entity.
     */
    public void testDiffGetterSetterEntity() {
        final Collection<DiffGetterSetterEntity> collection = collectionFactory.newInstance(DiffGetterSetterEntity.class);
        final DiffGetterSetterEntity entity = EntityFactory.newInstance(DiffGetterSetterEntity.class);

        try {
            collection.save(entity);
            fail("throw CodecConfigurationException");
        } catch (Throwable ex) {
            assertCodecNotFound(ex);
        }
    }

    /**
     * 测试只有setter的Entity.
     */
    public void testOnlySetterEntity() {
        final Collection<OnlySetterEntity> collection = collectionFactory.newInstance(OnlySetterEntity.class);
        final OnlySetterEntity entity = EntityFactory.newInstance(OnlySetterEntity.class);

        try {
            collection.save(entity);
            fail("throw CodecConfigurationException");
        } catch (Throwable ex) {
            assertCodecNotFound(ex);
        }
    }

    /**
     * 测试只有getter的Entity.
     */
    public void testOnlyGetterEntity() {
        final Collection<OnlyGetterEntity> collection = collectionFactory.newInstance(OnlyGetterEntity.class);
        final OnlyGetterEntity entity = EntityFactory.newInstance(OnlyGetterEntity.class);

        try {
            collection.save(entity);
            fail("throw CodecConfigurationException");
        } catch (Throwable ex) {
            assertCodecNotFound(ex);
        }
    }

    /**
     * 测试直接实现的方法级范型的Entity.
     */
    public void testDirectMethodGenericEntity() {
        final Collection<DirectMethodGenericEntity> collection = collectionFactory
                .newInstance(DirectMethodGenericEntity.class);
        final DirectMethodGenericEntity entity = EntityFactory.newInstance(DirectMethodGenericEntity.class);

        try {
            collection.save(entity);
            fail("throw CodecConfigurationException");
        } catch (Throwable ex) {
            assertCodecNotFound(ex);
        }
    }

    /**
     * 测试继承实现的方法级范型的Entity.
     */
    public void testInheritMethodGenericEntity() {
        final Collection<InheritMethodGenericEntity> collection = collectionFactory
                .newInstance(InheritMethodGenericEntity.class);
        final InheritMethodGenericEntity entity = EntityFactory.newInstance(InheritMethodGenericEntity.class);

        try {
            collection.save(entity);
            fail("throw CodecConfigurationException");
        } catch (Throwable ex) {
            assertCodecNotFound(ex);
        }
    }

    /**
     * 测试继承实现的方法级范型的Entity.
     */
    public void testInheritMethodGenericHidingEntity() {
        final Collection<InheritMethodGenericHidingEntity> collection = collectionFactory
                .newInstance(InheritMethodGenericHidingEntity.class);
        final InheritMethodGenericHidingEntity entity = EntityFactory.newInstance(InheritMethodGenericHidingEntity.class);

        try {
            collection.save(entity);
            fail("throw CodecConfigurationException");
        } catch (Throwable ex) {
            assertCodecNotFound(ex);
        }
    }

    private void assertCodecNotFound(Throwable ex) {
        assertEquals(CodecConfigurationException.class, ex.getClass());
        assertTrue(ex.getMessage().startsWith("Can't find a codec for"));
        assertNull(ex.getCause());
    }
}
