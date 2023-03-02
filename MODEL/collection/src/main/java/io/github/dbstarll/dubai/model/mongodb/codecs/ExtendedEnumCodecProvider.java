package io.github.dbstarll.dubai.model.mongodb.codecs;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

/**
 * A codec provider for classes that extend {@link Enum}.
 *
 * @since 1.1.3
 */
public final class ExtendedEnumCodecProvider implements CodecProvider {
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T> Codec<T> get(final Class<T> clazz, final CodecRegistry registry) {
        if (Enum.class.isAssignableFrom(clazz)) {
            return (Codec<T>) new ExtendedEnumCodec(clazz);
        }
        return null;
    }
}
