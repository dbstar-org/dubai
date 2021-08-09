package io.github.dbstarll.dubai.model.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.EntityFactory.PojoFields;
import io.github.dbstarll.utils.lang.EncryptUtils;
import io.github.dbstarll.utils.lang.bytes.Bytes;
import io.github.dbstarll.utils.lang.bytes.BytesUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.*;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.Convention;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.EntityConvention;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.lang.reflect.Proxy;
import java.util.LinkedList;
import java.util.List;

public final class MongoClientFactory {
    private final Bytes encryptedKey;

    public MongoClientFactory() {
        this(null);
    }

    public MongoClientFactory(Bytes encryptedKey) {
        this.encryptedKey = encryptedKey;
    }

    /**
     * 创建一个开启了PojoCodec特性的MongoClient实例.
     *
     * @param mongoUri mongodb的连接uri
     * @return 返回创建的MongoClient对象
     */
    public MongoClient createWithPojoCodec(String mongoUri) {
        return MongoClients.create(getMongoClientSettingsbuilder().applyConnectionString(new ConnectionString(mongoUri)).build());
    }

    /**
     * 通过分离的参数来配置MongoClient.
     *
     * @param servers             服务器信息
     * @param defaultAuthDatabase 默认的用户认证数据库
     * @param credential          认证信息
     * @return 返回创建的MongoClient对象
     */
    public MongoClient createWithPojoCodecSplit(final String servers, final String defaultAuthDatabase,
                                                final String credential) {
        return createWithPojoCodecSplit(servers, defaultAuthDatabase, credential, getMongoClientSettingsbuilder());
    }

    /**
     * 通过分离的参数来配置MongoClient.
     *
     * @param servers             服务器信息
     * @param defaultAuthDatabase 默认的用户认证数据库
     * @param credential          认证信息
     * @param settings            MongoClientSettings.Builder
     * @return 返回创建的MongoClient对象
     */
    public MongoClient createWithPojoCodecSplit(final String servers, final String defaultAuthDatabase,
                                                final String credential, final MongoClientSettings.Builder settings) {
        final MongoCredential mongoCredential = parseCredential(defaultAuthDatabase, credential);
        if (mongoCredential != null) {
            settings.credential(mongoCredential);
        }
        return MongoClients.create(settings.applyToClusterSettings(s -> {
            s.hosts(parseServers(servers));
        }).build());
    }

    /**
     * 构建MongoClientSettings.
     *
     * @return MongoClientSettings.Builder
     */
    public MongoClientSettings.Builder getMongoClientSettingsbuilder() {
        final List<Convention> conventions = new LinkedList<>();
        conventions.add(new EntityConvention());
        conventions.addAll(Conventions.DEFAULT_CONVENTIONS);
        final CodecProvider pojoCodecProvider = PojoCodecProvider.builder().conventions(conventions).automatic(true)
                .build();
        final CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(
                CodecRegistries.fromProviders(new DefaultCodecProvider()), MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(new EntityCodecProvider(), pojoCodecProvider));
        return MongoClientSettings.builder().codecRegistry(new DebugCodecRegistry(pojoCodecRegistry));
    }

    private static List<ServerAddress> parseServers(final String servers) {
        final List<ServerAddress> seeds = new LinkedList<ServerAddress>();
        for (String server : StringUtils.split(servers, ',')) {
            final int index = server.indexOf(':');
            if (index < 0) {
                seeds.add(new ServerAddress(server));
            } else {
                seeds.add(new ServerAddress(server.substring(0, index), Integer.parseInt(server.substring(index + 1))));
            }
        }
        return seeds;
    }

    private static MongoCredential parseCredential(final String defaultAuthDatabase, final String credential) {
        final String[] ps = StringUtils.splitPreserveAllTokens(credential, ":", 3);
        if (ps != null && ps.length == 3) {
            final String authDatabase = StringUtils.isBlank(ps[0]) ? defaultAuthDatabase : ps[0];
            final String userName = ps[1];
            final char[] password = ps[2].toCharArray();
            return MongoCredential.createScramSha1Credential(userName, authDatabase, password);
        }
        return null;
    }

    class DefaultCodecProvider implements CodecProvider {
        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
            if (Enum.class.isAssignableFrom(clazz)) {
                return new EnumCodec((Class<Enum<?>>) clazz);
            } else if (byte[].class.isAssignableFrom(clazz)) {
                return (Codec<T>) new ImageCodec();
            }
            return null;
        }
    }

    static class EntityCodecProvider implements CodecProvider {
        @SuppressWarnings("unchecked")
        @Override
        public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
            if (Entity.class.isAssignableFrom(clazz) && isProxy(clazz)) {
                return (Codec<T>) new EntityCodec<>((Class<? extends Entity>) clazz, registry);
            }
            return null;
        }

        private <T> boolean isProxy(Class<T> clazz) {
            return Proxy.isProxyClass(clazz) && PojoFields.class.isAssignableFrom(clazz);
        }
    }

    static class EntityCodec<E extends Entity> implements Codec<E> {
        private final Class<E> entityClass;
        private final MapCodec codec;

        EntityCodec(Class<E> entityClass, CodecRegistry registry) {
            this.entityClass = entityClass;
            this.codec = new MapCodec(registry);
        }

        @Override
        public void encode(BsonWriter writer, E value, EncoderContext encoderContext) {
            codec.encode(writer, ((PojoFields) value).fields(), encoderContext);
        }

        @Override
        public E decode(BsonReader reader, DecoderContext decoderContext) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Class<E> getEncoderClass() {
            return entityClass;
        }
    }

    static class EnumCodec<T extends Enum<T>> implements Codec<T> {
        private final Class<T> clazz;

        EnumCodec(final Class<T> clazz) {
            this.clazz = clazz;
        }

        @Override
        public void encode(final BsonWriter writer, final T value, final EncoderContext encoderContext) {
            writer.writeString(value.name());
        }

        @Override
        public Class<T> getEncoderClass() {
            return clazz;
        }

        @Override
        public T decode(final BsonReader reader, final DecoderContext decoderContext) {
            try {
                return Enum.valueOf(clazz, reader.readString());
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }
    }

    class ImageCodec extends ByteArrayCodec {
        private final Bytes jpegHeader = new Bytes(BytesUtils.decodeHexString("ffd8"));
        private final Bytes pngHeader = new Bytes(BytesUtils.decodeHexString("89504e470d0a1a0a"));

        @Override
        public void encode(BsonWriter writer, byte[] value, EncoderContext encoderContext) {
            super.encode(writer, encodeImage(value), encoderContext);
        }

        @Override
        public byte[] decode(BsonReader reader, DecoderContext decoderContext) {
            return decodeImage(super.decode(reader, decoderContext));
        }

        private byte[] encodeImage(final byte[] value) {
            return encryptedKey == null ? value : EncryptUtils.encryptCopy(value, encryptedKey);
        }

        private byte[] decodeImage(final byte[] value) {
            if (encryptedKey != null && !isImage(value, jpegHeader) && !isImage(value, pngHeader)) {
                return EncryptUtils.encryptCopy(value, encryptedKey);
            }
            return value;
        }

        private boolean isImage(final byte[] value, Bytes header) {
            return value.length > header.length() && header.compareTo(new Bytes(value, 0, header.length())) == 0;
        }
    }
}
