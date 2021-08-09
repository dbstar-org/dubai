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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.NoSuchAlgorithmException;

@Configuration
public class MongoAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(MongoClientFactory.class)
    MongoClientFactory mongoClientFactory(@Value("${dubai.mongodb.encryptedKey:}") String encryptedKey)
            throws NoSuchAlgorithmException {
        return new MongoClientFactory(
                StringUtils.isBlank(encryptedKey) ? null : new Bytes(EncryptUtils.sha(encryptedKey, 256)));
    }

    @Bean
    @ConditionalOnMissingBean(MongoClientSettings.Builder.class)
    MongoClientSettings.Builder clientOptions(MongoClientFactory mongoClientFactory) {
        return mongoClientFactory.getMongoClientSettingsbuilder();
    }

    @Bean
    @ConditionalOnMissingBean(MongoClient.class)
    MongoClient mongoClient(MongoClientFactory mongoClientFactory, MongoProperties mongoProperties,
                            MongoClientSettings.Builder options) {
        final String authDatabase = StringUtils.isNotBlank(mongoProperties.getAuthenticationDatabase())
                ? mongoProperties.getAuthenticationDatabase()
                : mongoProperties.getMongoClientDatabase();
        final String credential;
        if (StringUtils.isBlank(mongoProperties.getUsername()) || null == mongoProperties.getPassword()) {
            credential = null;
        } else {
            credential = StringUtils.join(
                    new Object[]{authDatabase, mongoProperties.getUsername(), new String(mongoProperties.getPassword())}, ':');
        }
        return mongoClientFactory.createWithPojoCodecSplit(mongoProperties.getHost(), authDatabase, credential, options);
    }

    @Bean
    @ConditionalOnMissingBean(MongoDatabase.class)
    MongoDatabase mongoDatabase(MongoClient client, MongoProperties properties) {
        return client.getDatabase(properties.getMongoClientDatabase());
    }
}
