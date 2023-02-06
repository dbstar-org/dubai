package io.github.dbstarll.dubai.model.spring.autoconfigure;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import io.github.dbstarll.dubai.model.mongodb.MongoClientFactory;
import io.github.dbstarll.utils.lang.bytes.Bytes;
import io.github.dbstarll.utils.lang.digest.Sha256Digestor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

@AutoConfiguration
@AutoConfigureAfter(MongoAutoConfiguration.class)
public class DatabaseAutoConfiguration {
    /**
     * 注入一个MongoClientSettingsBuilderCustomizer实例，用于定制化加入所需要的CodecRegistry.
     *
     * @param settings     初始的MongoClientSettings
     * @param encryptedKey 加密用的key
     * @return MongoClientSettingsBuilderCustomizer
     * @throws NoSuchAlgorithmException 加密key用的摘要算法不存在时抛出
     */
    @Bean
    @ConditionalOnBean(MongoClientSettings.class)
    MongoClientSettingsBuilderCustomizer mongoCodecRegistryCustomizer(
            final MongoClientSettings settings, @Value("${dubai.mongodb.encryptedKey:}") final String encryptedKey)
            throws NoSuchAlgorithmException {
        final Bytes encryptedBytes = StringUtils.isBlank(encryptedKey) ? null : new Bytes(
                new Sha256Digestor().digest(encryptedKey.getBytes(StandardCharsets.UTF_8)));
        return builder -> new MongoClientFactory(encryptedBytes).customize(builder, settings.getCodecRegistry());
    }

    /**
     * 自动装配MongoDatabase实例.
     *
     * @param client     MongoClient
     * @param properties 通过配置文件加载的配置参数
     * @return MongoDatabase实例
     */
    @Bean
    @ConditionalOnBean(MongoClient.class)
    @ConditionalOnMissingBean(MongoDatabase.class)
    MongoDatabase mongoDatabase(final MongoClient client, final MongoProperties properties) {
        return client.getDatabase(properties.getMongoClientDatabase());
    }
}
