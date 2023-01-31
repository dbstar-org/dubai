package io.github.dbstarll.dubai.model.mongodb.codecs;

import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.EntityFactory.PojoFields;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.MapCodec;
import org.bson.codecs.configuration.CodecRegistry;

public class EntityCodec<E extends Entity> implements Codec<E> {
    private final Class<E> entityClass;
    private final MapCodec codec;

    /**
     * 构造一个持久化Entity专用的Codec.
     *
     * @param entityClass 实体类
     * @param registry    外部的registry
     */
    public EntityCodec(final Class<E> entityClass, final CodecRegistry registry) {
        this.entityClass = entityClass;
        this.codec = new MapCodec(registry);
    }

    @Override
    public void encode(final BsonWriter writer, final E value, final EncoderContext encoderContext) {
        codec.encode(writer, ((PojoFields) value).fields(), encoderContext);
    }

    @Override
    public E decode(final BsonReader reader, final DecoderContext decoderContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<E> getEncoderClass() {
        return entityClass;
    }
}
