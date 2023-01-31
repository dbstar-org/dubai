package io.github.dbstarll.dubai.model.mongodb.codecs;

import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.EntityFactory.PojoFields;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

import java.lang.reflect.Proxy;

public class EntityCodecProvider implements CodecProvider {
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
