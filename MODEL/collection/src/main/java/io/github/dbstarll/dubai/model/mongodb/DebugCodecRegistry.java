package io.github.dbstarll.dubai.model.mongodb;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DebugCodecRegistry implements CodecRegistry {
    private static final ConcurrentMap<Class<?>, String> map = new ConcurrentHashMap<>();

    private final CodecRegistry registry;

    public DebugCodecRegistry(CodecRegistry registry) {
        this.registry = registry;
    }

    @Override
    public <T> Codec<T> get(Class<T> clazz) {
        final Codec<T> codec = registry.get(clazz);
        map.computeIfAbsent(clazz, c -> {
            if (codec == null) return null;
            else {
                System.out.println(c + " -> " + codec);
                return codec.toString();
            }
        });
        return codec;
    }

    @Override
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
        return this.registry.get(clazz, registry);
    }
}
