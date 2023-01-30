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

    @Bean
    @ConditionalOnMissingBean(MongoClientFactory.class)
    static MongoClientFactory mongoClientFactory(@Value("${dubai.mongodb.encryptedKey:}") final String encryptedKey)
            throws NoSuchAlgorithmException {
        return new MongoClientFactory(
                StringUtils.isBlank(encryptedKey) ? null : new Bytes(EncryptUtils.sha(encryptedKey, SHA_STRENGTH)));
    }

    @Bean
    @ConditionalOnMissingBean(MongoClientSettings.Builder.class)
    static MongoClientSettings.Builder clientOptions(final MongoClientFactory mongoClientFactory) {
        return mongoClientFactory.getMongoClientSettingsbuilder();
    }

    @Bean
    @ConditionalOnMissingBean(MongoClient.class)
    static MongoClient mongoClient(final MongoClientFactory mongoClientFactory, final MongoProperties mongoProperties,
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

    @Bean
    @ConditionalOnMissingBean(MongoDatabase.class)
    static MongoDatabase mongoDatabase(final MongoClient client, final MongoProperties properties) {
        return client.getDatabase(properties.getMongoClientDatabase());
    }


    private static <V> V getOrDefault(final V value, final V defaultValue) {
        return (value != null) ? value : defaultValue;
    }
}
