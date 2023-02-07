package io.github.dbstarll.dubai.model.service;

import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoCollection;
import io.github.dbstarll.dubai.model.collection.BaseCollection;
import io.github.dbstarll.dubai.model.entity.Entity;
import org.bson.BsonReader;
import org.bson.codecs.DecoderContext;

public interface ServiceHelper<E extends Entity, S extends Service<E>> {
    /**
     * 从封装的CollectionWrapper中找到原始的BaseCollection.
     *
     * @return 原始的BaseCollection
     */
    BaseCollection<E> getBaseCollection();

    /**
     * Decodes a BSON value from the given reader into an instance of the entity.
     *
     * @param reader         the BSON reader
     * @param decoderContext the decoder context
     * @return an instance of the entity.
     */
    E decode(BsonReader reader, DecoderContext decoderContext);

    /**
     * 获得MongoCollection实例.
     *
     * @return MongoCollection
     */
    default MongoCollection<E> getMongoCollection() {
        return getBaseCollection().getMongoCollection();
    }

    /**
     * Gets the namespace of this collection.
     *
     * @return the namespace
     */
    default MongoNamespace getNamespace() {
        return getMongoCollection().getNamespace();
    }
}
