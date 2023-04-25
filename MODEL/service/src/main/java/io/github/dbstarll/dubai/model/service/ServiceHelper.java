package io.github.dbstarll.dubai.model.service;

import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Variable;
import io.github.dbstarll.dubai.model.collection.BaseCollection;
import io.github.dbstarll.dubai.model.entity.Entity;
import org.bson.BsonArray;
import org.bson.BsonReader;
import org.bson.codecs.DecoderContext;
import org.bson.conversions.Bson;

import java.util.List;
import java.util.stream.Collectors;

public interface ServiceHelper<E extends Entity> {
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
     * Decodes an array of BSON values into a list of the entity.
     *
     * @param array          array of BsonDocument
     * @param decoderContext the decoder context
     * @return a list of entity.
     */
    default List<E> decode(BsonArray array, DecoderContext decoderContext) {
        return array.stream().map(d -> decode(d.asDocument().asBsonReader(), decoderContext))
                .collect(Collectors.toList());
    }

    /**
     * Decodes an array of BSON values and return the first entity.
     *
     * @param array          array of BsonDocument
     * @param decoderContext the decoder context
     * @return the first entity or null if the array is empty
     */
    default E decodeOne(BsonArray array, DecoderContext decoderContext) {
        return array.isEmpty() ? null : decode(array.get(0).asDocument().asBsonReader(), decoderContext);
    }

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

    /**
     * Creates a $lookup pipeline stage, joining the current collection with the one specified in from
     * using equality match between the local field and the foreign field.
     *
     * @param localField   the field from the local collection to match values against.
     * @param foreignField the field in the from collection to match values against.
     * @param as           the name of the new array field to add to the input documents.
     * @return the $lookup pipeline stage
     */
    default Bson lookup(String localField, String foreignField, String as) {
        return Aggregates.lookup(getNamespace().getCollectionName(), localField, foreignField, as);
    }

    /**
     * Creates a $lookup pipeline stage, joining the current collection with the one specified in from
     * using the given pipeline.
     *
     * @param <V>      the Variable value expression type
     * @param let      the variables to use in the pipeline field stages.
     * @param pipeline the pipeline to run on the joined collection.
     * @param as       the name of the new array field to add to the input documents.
     * @return the $lookup pipeline stage
     */
    default <V> Bson lookup(List<Variable<V>> let, List<? extends Bson> pipeline, String as) {
        return Aggregates.lookup(getNamespace().getCollectionName(), let, pipeline, as);
    }
}
