package io.github.dbstarll.dubai.model.mongodb.codecs;

import io.github.dbstarll.utils.lang.EncryptUtils;
import io.github.dbstarll.utils.lang.bytes.Bytes;
import io.github.dbstarll.utils.lang.bytes.BytesUtils;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.ByteArrayCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class EncryptedByteArrayCodec extends ByteArrayCodec {
    private static final Bytes HEADER_JPEG = new Bytes(BytesUtils.decodeHexString("ffd8"));
    private static final Bytes HEADER_PNG = new Bytes(BytesUtils.decodeHexString("89504e470d0a1a0a"));

    private final Bytes encryptedKey;
    private final Set<Bytes> ignoreHeaders;

    /**
     * 构造一个有加密功能的字节数组Codec，若提供的密钥为空，则不加密.
     *
     * @param encryptedKey 加密密钥
     */
    public EncryptedByteArrayCodec(final Bytes encryptedKey) {
        this.encryptedKey = encryptedKey;
        this.ignoreHeaders = new HashSet<>(Arrays.asList(HEADER_JPEG, HEADER_PNG));
    }

    @Override
    public void encode(final BsonWriter writer, final byte[] value, final EncoderContext encoderContext) {
        super.encode(writer, encodeBytes(value), encoderContext);
    }

    @Override
    public byte[] decode(final BsonReader reader, final DecoderContext decoderContext) {
        return decodeBytes(super.decode(reader, decoderContext));
    }

    private byte[] encodeBytes(final byte[] value) {
        return encryptedKey == null ? value : EncryptUtils.encryptCopy(value, encryptedKey);
    }

    private byte[] decodeBytes(final byte[] value) {
        if (encryptedKey == null || ignoreHeaders.stream().anyMatch(header -> matchHeader(value, header))) {
            return value;
        } else {
            return EncryptUtils.encryptCopy(value, encryptedKey);
        }
    }

    private boolean matchHeader(final byte[] value, final Bytes header) {
        return value.length > header.length() && header.compareTo(new Bytes(value, 0, header.length())) == 0;
    }
}