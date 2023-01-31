package io.github.dbstarll.dubai.model.mongodb.codecs;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

public final class NullableEnumCodec<T extends Enum<T>> implements Codec<T> {
    private final Class<T> clazz;

    /**
     * Construct an instance for teh given enum class.
     *
     * @param clazz the enum class
     */
    public NullableEnumCodec(final Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void encode(final BsonWriter writer, final T value, final EncoderContext encoderContext) {
        writer.writeString(value.name());
    }

    @Override
    public Class<T> getEncoderClass() {
        return clazz;
    }

    @Override
    public T decode(final BsonReader reader, final DecoderContext decoderContext) {
        try {
            return Enum.valueOf(clazz, reader.readString());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
