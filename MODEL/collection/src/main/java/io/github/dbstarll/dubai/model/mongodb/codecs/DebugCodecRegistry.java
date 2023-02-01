package io.github.dbstarll.dubai.model.mongodb.codecs;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebugCodecRegistry implements CodecRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(DebugCodecRegistry.class);

    private final CodecRegistry registry;

    /**
     * 用于打印调试信息的CodecRegistry.
     *
     * @param registry 被封装的CodecRegistry
     */
    public DebugCodecRegistry(final CodecRegistry registry) {
        this.registry = registry;
    }

    @Override
    public <T> Codec<T> get(final Class<T> clazz) {
        return debug(clazz, registry.get(clazz));
    }

    @Override
    public <T> Codec<T> get(final Class<T> clazz, final CodecRegistry dependentRegistry) {
        return debug(clazz, registry.get(clazz, dependentRegistry));
    }

    private <T> Codec<T> debug(final Class<T> clazz, final Codec<T> codec) {
        LOGGER.debug("{} -> {}", clazz, codec);
        return codec;
    }
}
