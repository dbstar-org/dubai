package io.github.dbstarll.dubai.model.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoClientSettings.Builder;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.EntityFactory.PojoFields;
import io.github.dbstarll.dubai.model.mongodb.codecs.EncryptedByteArrayCodec;
import io.github.dbstarll.dubai.model.mongodb.codecs.NullableEnumCodec;
import io.github.dbstarll.utils.lang.bytes.Bytes;
import org.apache.commons.lang3.StringUtils;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.MapCodec;
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
    private static final int CREDENTIAL_FIELD_COUNT = 3;

    private final Bytes encryptedKey;

    /**
     * 构造一个无加密的MongoClient工厂类.
     */
    public MongoClientFactory() {
        this(null);
    }

    /**
     * 构造一个加密的MongoClient工厂类，并指定加密密钥.
     *
     * @param encryptedKey 加密密钥
     */
    public MongoClientFactory(final Bytes encryptedKey) {
        this.encryptedKey = encryptedKey;
    }

    /**
     * 创建一个开启了PojoCodec特性的MongoClient实例.
     *
     * @param mongoUri mongodb的连接uri
     * @return 返回创建的MongoClient对象
     */
    public MongoClient createWithPojoCodec(final String mongoUri) {
        return MongoClients.create(getMongoClientSettingsBuilder()
                .applyConnectionString(new ConnectionString(mongoUri))
                .build());
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
        return createWithPojoCodecSplit(servers, defaultAuthDatabase, credential, getMongoClientSettingsBuilder());
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
        return MongoClients.create(settings.applyToClusterSettings(s -> s.hosts(parseServers(servers))).build());
    }

    /**
     * 构建MongoClientSettings.
     *
     * @return MongoClientSettings.Builder
     */
    public Builder getMongoClientSettingsBuilder() {
        return customize(MongoClientSettings.builder());
    }

    /**
     * 定制clientSettingsBuilder.
     *
     * @param clientSettingsBuilder clientSettingsBuilder
     * @return 定制后的clientSettingsBuilder
     */
    public Builder customize(final Builder clientSettingsBuilder) {
        final List<Convention> conventions = new LinkedList<>();
        conventions.add(new EntityConvention());
        conventions.addAll(Conventions.DEFAULT_CONVENTIONS);
        final CodecProvider pojoCodecProvider = PojoCodecProvider.builder()
                .conventions(conventions).automatic(true).build();
        final CodecRegistry defaultCodecRegistry = MongoClientSettings.getDefaultCodecRegistry();
        final CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(
                CodecRegistries.fromProviders(new DefaultCodecProvider()), defaultCodecRegistry,
                CodecRegistries.fromProviders(new EntityCodecProvider(), pojoCodecProvider));
        clientSettingsBuilder.codecRegistry(new DebugCodecRegistry(pojoCodecRegistry));
        return clientSettingsBuilder;
    }

    private static List<ServerAddress> parseServers(final String servers) {
        final List<ServerAddress> seeds = new LinkedList<>();
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
        final String[] ps = StringUtils.splitPreserveAllTokens(credential, ":", CREDENTIAL_FIELD_COUNT);
        if (ps != null && ps.length == CREDENTIAL_FIELD_COUNT) {
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
        public <T> Codec<T> get(final Class<T> clazz, final CodecRegistry registry) {
            if (Enum.class.isAssignableFrom(clazz)) {
                return new NullableEnumCodec(clazz);
            } else if (byte[].class.isAssignableFrom(clazz)) {
                return (Codec<T>) new EncryptedByteArrayCodec(encryptedKey);
            }
            return null;
        }
    }

    static class EntityCodecProvider implements CodecProvider {
        @SuppressWarnings("unchecked")
        @Override
        public <T> Codec<T> get(final Class<T> clazz, final CodecRegistry registry) {
            if (Entity.class.isAssignableFrom(clazz) && isProxy(clazz)) {
                return (Codec<T>) new EntityCodec<>((Class<? extends Entity>) clazz, registry);
            }
            return null;
        }

        private <T> boolean isProxy(final Class<T> clazz) {
            return Proxy.isProxyClass(clazz) && PojoFields.class.isAssignableFrom(clazz);
        }
    }

    static class EntityCodec<E extends Entity> implements Codec<E> {
        private final Class<E> entityClass;
        private final MapCodec codec;

        EntityCodec(final Class<E> entityClass, final CodecRegistry registry) {
            this.entityClass = entityClass;
            this.codec = new MapCodec(registry);
        }

        @Override
        public void encode(final BsonWriter writer, final E value, final EncoderContext encoderContext) {
            codec.encode(writer, ((PojoFields) value).fields(), encoderContext);
        }

        @Override
        public E decode(final BsonReader reader, final DecoderContext decoderContext) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Class<E> getEncoderClass() {
            return entityClass;
        }
    }
}
