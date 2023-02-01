package io.github.dbstarll.dubai.model.mongodb.codecs;

import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class EntityCodecProvider implements CodecProvider {
    @SuppressWarnings("unchecked")
    @Override
    public <T> Codec<T> get(final Class<T> clazz, final CodecRegistry registry) {
        if (EntityFactory.isEntityProxy(clazz)) {
            return (Codec<T>) new EntityCodec<>((Class<? extends Entity>) clazz, registry);
        }
        return null;
    }
}
