package io.github.dbstarll.dubai.model.spring.autoconfigure;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import io.github.dbstarll.dubai.model.mongodb.MongoClientFactory;
import io.github.dbstarll.utils.lang.EncryptUtils;
import io.github.dbstarll.utils.lang.bytes.Bytes;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.NoSuchAlgorithmException;

@Configuration
@EnableConfigurationProperties(MongoProperties.class)
public class MongoAutoConfiguration {
    private static final int SHA_STRENGTH = 256;

    /**
     * 自动装配MongoClientFactory实例，用于配置并生成MongoClient.
     *
     * @param encryptedKey 加密用的key
     * @return MongoClientFactory实例
     * @throws NoSuchAlgorithmException 加密key用的摘要算法不存在时抛出
     */
    @Bean
    @ConditionalOnMissingBean(MongoClientFactory.class)
    MongoClientFactory mongoClientFactory(@Value("${dubai.mongodb.encryptedKey:}") final String encryptedKey)
            throws NoSuchAlgorithmException {
        return new MongoClientFactory(
                StringUtils.isBlank(encryptedKey) ? null : new Bytes(EncryptUtils.sha(encryptedKey, SHA_STRENGTH)));
    }

    /**
     * 自动装配MongoClientSettings.Builder实例，用于配置MongoClient的各种参数.
     *
     * @param mongoClientFactory mongoClientFactory实例
     * @return MongoClientSettings.Builder实例
     */
    @Bean
    @ConditionalOnMissingBean(MongoClientSettings.Builder.class)
    MongoClientSettings.Builder mongoClientSettingsBuilder(final MongoClientFactory mongoClientFactory) {
        return mongoClientFactory.getMongoClientSettingsBuilder();
    }

    /**
     * 自动装配MongoClient实例.
     *
     * @param mongoClientFactory mongoClientFactory实例
     * @param mongoProperties    通过配置文件加载的配置参数
     * @param options            通过内部API加载的配置参数
     * @return MongoClient实例
     */
    @Bean
    @ConditionalOnMissingBean(MongoClient.class)
    MongoClient mongoClient(final MongoClientFactory mongoClientFactory, final MongoProperties mongoProperties,
                            final MongoClientSettings.Builder options) {
        final String authDB = StringUtils.isNotBlank(mongoProperties.getAuthenticationDatabase())
                ? mongoProperties.getAuthenticationDatabase()
                : mongoProperties.getMongoClientDatabase();
        final String credential;
        if (StringUtils.isBlank(mongoProperties.getUsername()) || null == mongoProperties.getPassword()) {
            credential = null;
        } else {
            credential = StringUtils.join(
                    new Object[]{authDB, mongoProperties.getUsername(), new String(mongoProperties.getPassword())},
                    ':');
        }
        return mongoClientFactory.createWithPojoCodecSplit(
                getOrDefault(mongoProperties.getHost(), "localhost"), authDB, credential, options);
    }

    /**
     * 自动装配MongoDatabase实例.
     *
     * @param client     MongoClient
     * @param properties 通过配置文件加载的配置参数
     * @return MongoDatabase实例
     */
    @Bean
    @ConditionalOnMissingBean(MongoDatabase.class)
    MongoDatabase mongoDatabase(final MongoClient client, final MongoProperties properties) {
        return client.getDatabase(properties.getMongoClientDatabase());
    }

    private static <V> V getOrDefault(final V value, final V defaultValue) {
        return value != null ? value : defaultValue;
    }
}
