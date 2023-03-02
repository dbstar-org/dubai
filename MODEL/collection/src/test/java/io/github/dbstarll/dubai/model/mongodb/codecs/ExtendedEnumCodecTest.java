package io.github.dbstarll.dubai.model.mongodb.codecs;

import com.mongodb.client.internal.MongoClientImpl;
import io.github.dbstarll.dubai.model.MongodTestCase;
import io.github.dbstarll.dubai.model.entity.test.enums.Custom;
import io.github.dbstarll.dubai.model.entity.test.enums.Default;
import io.github.dbstarll.dubai.model.entity.test.enums.Normal;
import io.github.dbstarll.dubai.model.entity.test.enums.ToString;
import org.bson.BsonDocument;
import org.bson.BsonDocumentReader;
import org.bson.BsonDocumentWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ExtendedEnumCodecTest extends MongodTestCase {
    @BeforeAll
    static void beforeClass() {
        globalCollectionFactory();
    }

    @Test
    void codec() {
        useClient(c -> {
            final CodecRegistry registry = ((MongoClientImpl) c).getCodecRegistry();
            testEnumCodec(registry, Normal.class, Enum::name);
            testEnumCodec(registry, Default.class, Enum::name);
            testEnumCodec(registry, Custom.class, Custom::getTitle);
            testEnumCodec(registry, ToString.class, Enum::toString);
        });
    }

    private <T extends Enum<T>> void testEnumCodec(final CodecRegistry registry,
                                                   final Class<T> enumClass, Function<T, String> value) {
        final Codec<T> codec = registry.get(enumClass);
        assertSame(ExtendedEnumCodec.class, codec.getClass());
        assertSame(enumClass, codec.getEncoderClass());
        Arrays.stream(enumClass.getEnumConstants()).forEach(e -> testEnumCodec(codec, e, value.apply(e)));
    }

    private <T extends Enum<T>> void testEnumCodec(final Codec<T> codec, final T test, final String expected) {
        final BsonDocument document = new BsonDocument();
        try (final BsonDocumentWriter writer = new BsonDocumentWriter(document)) {
            writer.writeStartDocument();
            writer.writeName("enum");
            codec.encode(writer, test, EncoderContext.builder().build());
            writer.writeEndDocument();
        }
        assertEquals(expected, document.getString("enum").getValue(),
                "encode failed on " + codec.getEncoderClass().getName());

        try (final BsonDocumentReader reader = new BsonDocumentReader(document)) {
            reader.readStartDocument();
            reader.readName();
            assertSame(test, codec.decode(reader, DecoderContext.builder().build()),
                    "decode failed on " + codec.getEncoderClass().getName());
        }
    }
}