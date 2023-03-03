package io.github.dbstarll.dubai.model.mongodb.codecs;

import io.github.dbstarll.utils.lang.enums.EnumUtils;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import static org.apache.commons.lang3.Validate.notEmpty;

/**
 * A codec for classes that extends {@link Enum}.
 *
 * @param <T> The enum type
 * @since 1.1.3
 */
public final class ExtendedEnumCodec<T extends Enum<T>> implements Codec<T> {
    private final Class<T> clazz;

    /**
     * Construct an instance for teh given enum class.
     *
     * @param clazz the enum class
     */
    public ExtendedEnumCodec(final Class<T> clazz) {
        this.clazz = clazz;
        notEmpty(EnumUtils.stream(clazz).toArray());
    }

    @Override
    public T decode(final BsonReader reader, final DecoderContext decoderContext) {
        return EnumUtils.valueOf(clazz, reader.readString());
    }

    @Override
    public void encode(final BsonWriter writer, final T value, final EncoderContext encoderContext) {
        writer.writeString(EnumUtils.name(value));
    }

    @Override
    public Class<T> getEncoderClass() {
        return clazz;
    }
}
