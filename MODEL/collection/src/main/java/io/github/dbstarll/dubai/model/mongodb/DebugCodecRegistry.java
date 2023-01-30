package io.github.dbstarll.dubai.model.mongodb;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DebugCodecRegistry implements CodecRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(DebugCodecRegistry.class);
    private static final ConcurrentMap<Class<?>, String> MAP = new ConcurrentHashMap<>();

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
        final Codec<T> codec = registry.get(clazz);
        MAP.computeIfAbsent(clazz, c -> {
            if (codec == null) {
                return null;
            } else {
                LOGGER.debug("{} -> {}", c, codec);
                return codec.toString();
            }
        });
        return codec;
    }

    @Override
    public <T> Codec<T> get(final Class<T> clazz, final CodecRegistry dependentRegistry) {
        return this.registry.get(clazz, dependentRegistry);
    }
}
