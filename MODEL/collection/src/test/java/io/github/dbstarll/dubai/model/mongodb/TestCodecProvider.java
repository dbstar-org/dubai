package io.github.dbstarll.dubai.model.mongodb;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.internal.MongoClientImpl;
import de.flapdoodle.embed.mongo.commands.ServerAddress;
import io.github.dbstarll.dubai.model.MongodTestCase;
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
import io.github.dbstarll.dubai.model.collection.test.o2.OverrideGetWithSubClassEntity;
import io.github.dbstarll.dubai.model.collection.test.o2.OverrideSetWithOtherClassEntity;
import io.github.dbstarll.dubai.model.collection.test.o2.OverrideSetWithSubClassEntity;
import io.github.dbstarll.dubai.model.collection.test.o2.OverrideSetWithSuperClassEntity;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.entity.EntityModifier;
import io.github.dbstarll.dubai.model.mongodb.codecs.EncryptedByteArrayCodec;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class TestCodecProvider extends MongodTestCase {
    @BeforeAll
    public static void beforeClass() {
        globalCollectionFactory();
    }

    /**
     * 测试基于接口的Entity.
     */
    @Test
    public void testInterfaceEntity() {
        useCollection(SimpleEntity.class, c -> {
            final SimpleEntity entity = EntityFactory.newInstance(SimpleEntity.class);
            entity.setType(Type.t1);
            entity.setBytes(new ObjectId().toByteArray());
            assertSame(entity, c.save(entity));
        });
    }

    /**
     * 测试基于Class的Entity.
     */
    @Test
    public void testClassEntity() {
        useCollection(SimpleClassEntity.class, c -> {
            final SimpleClassEntity entity = EntityFactory.newInstance(SimpleClassEntity.class);
            assertSame(entity, c.save(entity));
        });
    }

    /**
     * 测试未实现PojoFields的Proxy.
     */
    @Test
    public void testProxyNoPojoFields() {
        useCollection(SimpleEntity.class, c -> {
            final SimpleEntity entity = (SimpleEntity) Proxy.newProxyInstance(SimpleEntity.class.getClassLoader(),
                    new Class[]{SimpleEntity.class, EntityModifier.class}, (proxy, method, args) -> null);
            try {
                c.save(entity);
            } catch (Exception ex) {
                assertEquals(CodecConfigurationException.class, ex.getClass());
                assertNotNull(ex.getCause());
                assertEquals(CodecConfigurationException.class, ex.getCause().getClass());
            }
        });
    }

    /**
     * 测试保存一个非Entity的对象.
     */
    @Test
    public void testNoEntity() {
        useDatabase(db -> {
            final MongoCollection<NotEntity> collection = db.getCollection("oid", NotEntity.class);

            try {
                collection.insertOne(new NotEntity());
                fail("throw CodecConfigurationException");
            } catch (Throwable ex) {
                assertCodecNotFound(ex);
            }
        });
    }

    /**
     * 测试ImageCodec.
     */
    @Test
    public void testImageCodecEncode() {
        useClient(c -> {
            final CodecRegistry registry = ((MongoClientImpl) c).getCodecRegistry();
            final Codec<byte[]> codec = registry.get(byte[].class);
            assertSame(EncryptedByteArrayCodec.class, codec.getClass());
            assertSame(byte[].class, codec.getEncoderClass());

            testImageCodec(codec, "png.png", true, true);
            testImageCodec(codec, "jpg.jpg", true, true);
            testImageCodec(codec, "ico.ico", false, true);
            testImageCodec(codec, "txt.txt", false, true);
        });
    }

    /**
     * 测试ImageCodec.
     */
    @Test
    public void testImageCodecNotEncode() {
        useMongod(d -> {
            final ServerAddress serverAddress = d.current().getServerAddress();
            final MongoClientSettings settings = mongoClientSettings();
            final MongoClientSettingsBuilderCustomizer serverCustomizer = b -> b.applyToClusterSettings(
                    c -> c.hosts(Collections.singletonList(
                            new com.mongodb.ServerAddress(serverAddress.getHost(), serverAddress.getPort())
                    ))
            );
            try (final MongoClient client = new org.springframework.boot.autoconfigure.mongo.MongoClientFactory(
                    Arrays.asList(
                            b -> new io.github.dbstarll.dubai.model.mongodb.MongoClientFactory()
                                    .customize(b, settings.getCodecRegistry()),
                            serverCustomizer
                    )).createMongoClient(settings)) {
                final CodecRegistry registry = ((MongoClientImpl) client).getCodecRegistry();
                final Codec<byte[]> codec = registry.get(byte[].class);
                assertSame(EncryptedByteArrayCodec.class, codec.getClass());
                assertSame(byte[].class, codec.getEncoderClass());

                testImageCodec(codec, "png.png", true, false);
                testImageCodec(codec, "jpg.jpg", true, false);
                testImageCodec(codec, "ico.ico", false, false);
                testImageCodec(codec, "txt.txt", false, false);
            }
        });
    }

    private void testImageCodec(final Codec<byte[]> codec, String resource, boolean image, boolean encode) {
        final BsonDocument document = new BsonDocument();
        final BsonWriter writer = new BsonDocumentWriter(document);
        final byte[] data;
        try {
            data = read(resource);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
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
            assertNotNull(in);
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
    @Test
    public void testGenericEntity() {
        useCollection(SimpleGenericEntity.class, c -> {
            final SimpleGenericEntity entity = EntityFactory.newInstance(SimpleGenericEntity.class);
            entity.setKey("key");
            entity.setValue(100);
            assertSame(entity, c.save(entity));
        });
    }

    /**
     * 测试基于多个同名属性的Entity.
     */
    @Test
    public void testMultiSetterEntity() {
        useCollection(MultiSetterEntity.class, c -> {
            final MultiSetterEntity entity = EntityFactory.newInstance(MultiSetterEntity.class);
            entity.setData(true);
            entity.setData(100);

            try {
                c.save(entity);
                fail("throw CodecConfigurationException");
            } catch (Throwable ex) {
                assertCodecNotFound(ex);
            }
        });
    }

    /**
     * 测试属性的getter和setter类型不一致的Entity.
     */
    @Test
    public void testDiffGetterSetterEntity() {
        useCollection(DiffGetterSetterEntity.class, c -> {
            final DiffGetterSetterEntity entity = EntityFactory.newInstance(DiffGetterSetterEntity.class);

            try {
                c.save(entity);
                fail("throw CodecConfigurationException");
            } catch (Throwable ex) {
                assertCodecNotFound(ex);
            }
        });
    }

    /**
     * 测试只有setter的Entity.
     */
    @Test
    public void testOnlySetterEntity() {
        useCollection(OnlySetterEntity.class, c -> {
            final OnlySetterEntity entity = EntityFactory.newInstance(OnlySetterEntity.class);

            try {
                c.save(entity);
                fail("throw CodecConfigurationException");
            } catch (Throwable ex) {
                assertCodecNotFound(ex);
            }
        });
    }

    /**
     * 测试只有getter的Entity.
     */
    @Test
    public void testOnlyGetterEntity() {
        useCollection(OnlyGetterEntity.class, c -> {
            final OnlyGetterEntity entity = EntityFactory.newInstance(OnlyGetterEntity.class);

            try {
                c.save(entity);
                fail("throw CodecConfigurationException");
            } catch (Throwable ex) {
                assertCodecNotFound(ex);
            }
        });
    }

    /**
     * 测试直接实现的方法级范型的Entity.
     */
    @Test
    public void testDirectMethodGenericEntity() {
        useCollection(DirectMethodGenericEntity.class, c -> {
            final DirectMethodGenericEntity entity = EntityFactory.newInstance(DirectMethodGenericEntity.class);

            try {
                c.save(entity);
                fail("throw CodecConfigurationException");
            } catch (Throwable ex) {
                assertCodecNotFound(ex);
            }
        });
    }

    /**
     * 测试继承实现的方法级范型的Entity.
     */
    @Test
    public void testInheritMethodGenericEntity() {
        useCollection(InheritMethodGenericEntity.class, c -> {
            final InheritMethodGenericEntity entity = EntityFactory.newInstance(InheritMethodGenericEntity.class);

            try {
                c.save(entity);
                fail("throw CodecConfigurationException");
            } catch (Throwable ex) {
                assertCodecNotFound(ex);
            }
        });
    }

    /**
     * 测试继承实现的方法级范型的Entity.
     */
    @Test
    public void testInheritMethodGenericHidingEntity() {
        useCollection(InheritMethodGenericHidingEntity.class, c -> {
            final InheritMethodGenericHidingEntity entity = EntityFactory.newInstance(InheritMethodGenericHidingEntity.class);

            try {
                c.save(entity);
                fail("throw CodecConfigurationException");
            } catch (Throwable ex) {
                assertCodecNotFound(ex);
            }
        });
    }

    @Test
    public void testOverrideSetWithSubClassEntity() {
        useCollection(OverrideSetWithSubClassEntity.class, c -> {
            final OverrideSetWithSubClassEntity entity = EntityFactory.newInstance(OverrideSetWithSubClassEntity.class);
            assertEquals(entity, c.save(entity));
        });
    }

    @Test
    public void testOverrideSetWithSuperClassEntity() {
        useCollection(OverrideSetWithSuperClassEntity.class, c -> {
            final OverrideSetWithSuperClassEntity entity = EntityFactory.newInstance(OverrideSetWithSuperClassEntity.class);

            try {
                c.save(entity);
                fail("throw CodecConfigurationException");
            } catch (Throwable ex) {
                assertCodecNotFound(ex);
            }
        });
    }

    @Test
    public void testOverrideSetWithOtherClassEntity() {
        useCollection(OverrideSetWithOtherClassEntity.class, c -> {
            final OverrideSetWithOtherClassEntity entity = EntityFactory.newInstance(OverrideSetWithOtherClassEntity.class);

            try {
                c.save(entity);
                fail("throw CodecConfigurationException");
            } catch (Throwable ex) {
                assertCodecNotFound(ex);
            }
        });
    }

    @Test
    public void testOverrideGetWithSubClassEntity() {
        useCollection(OverrideGetWithSubClassEntity.class, c -> {
            final OverrideGetWithSubClassEntity entity = EntityFactory.newInstance(OverrideGetWithSubClassEntity.class);
            assertEquals(entity, c.save(entity));
        });
    }

    private void assertCodecNotFound(Throwable ex) {
        assertEquals(CodecConfigurationException.class, ex.getClass());
        assertTrue(ex.getMessage().startsWith("Can't find a codec for"));
        assertNull(ex.getCause());
    }
}
