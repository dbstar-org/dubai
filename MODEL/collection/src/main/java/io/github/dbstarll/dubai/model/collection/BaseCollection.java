package io.github.dbstarll.dubai.model.collection;

import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import io.github.dbstarll.dubai.model.entity.Entity;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;

import java.util.List;

public class BaseCollection<E extends Entity> extends AbstractCollection<E> {
    private final MongoCollection<E> mongoCollection;

    public BaseCollection(MongoCollection<E> mongoCollection) {
        this.mongoCollection = mongoCollection;
    }

    protected Bson queryFilter(Bson filter) {
        BsonDocument ret;
        if (null == filter) {
            ret = new BsonDocument();
        } else {
            ret = filter.toBsonDocument(this.getEntityClass(), this.mongoCollection.getCodecRegistry());
        }
        return ret;
    }

    @Override
    public long count(Bson filter, CountOptions options) {
        return mongoCollection.countDocuments(queryFilter(filter), options);
    }

    @Override
    public <T> DistinctIterable<T> distinct(String fieldName, Bson filter, Class<T> resultClass) {
        return mongoCollection.distinct(fieldName, queryFilter(filter), resultClass);
    }

    @Override
    public <T> FindIterable<T> find(Bson filter, Class<T> resultClass) {
        return mongoCollection.find(queryFilter(filter), resultClass);
    }

    @Override
    public DeleteResult deleteOne(Bson filter, DeleteOptions options) {
        return mongoCollection.deleteOne(filter, options);
    }

    @Override
    public DeleteResult deleteMany(Bson filter, DeleteOptions options) {
        return mongoCollection.deleteMany(filter, options);
    }

    @Override
    public UpdateResult replaceOne(Bson filter, E replacement, ReplaceOptions replaceOptions) {
        return mongoCollection.replaceOne(queryFilter(filter), replacement, replaceOptions);
    }

    @Override
    public UpdateResult updateOne(Bson filter, Bson update, UpdateOptions updateOptions) {
        return mongoCollection.updateOne(queryFilter(filter), update, updateOptions);
    }

    @Override
    public UpdateResult updateMany(Bson filter, Bson update, UpdateOptions updateOptions) {
        return mongoCollection.updateMany(queryFilter(filter), update, updateOptions);
    }

    @Override
    public E findOneAndDelete(Bson filter, FindOneAndDeleteOptions options) {
        return mongoCollection.findOneAndDelete(queryFilter(filter), options);
    }

    @Override
    public E findOneAndReplace(Bson filter, E replacement, FindOneAndReplaceOptions options) {
        return mongoCollection.findOneAndReplace(queryFilter(filter), replacement, options);
    }

    @Override
    public E findOneAndUpdate(Bson filter, Bson update, FindOneAndUpdateOptions options) {
        return mongoCollection.findOneAndUpdate(queryFilter(filter), update, options);
    }

    @Override
    public Class<E> getEntityClass() {
        return mongoCollection.getDocumentClass();
    }

    @Override
    public <T> AggregateIterable<T> aggregate(List<? extends Bson> pipeline, Class<T> resultClass) {
        return mongoCollection.aggregate(pipeline, resultClass);
    }

    @Override
    public <T> MapReduceIterable<T> mapReduce(String mapFunction, String reduceFunction, Class<T> resultClass) {
        return mongoCollection.mapReduce(mapFunction, reduceFunction, resultClass);
    }

    @Override
    public void insertOne(E document, InsertOneOptions options) {
        mongoCollection.insertOne(document, options);
    }

    @Override
    public void insertMany(List<? extends E> documents, InsertManyOptions options) {
        mongoCollection.insertMany(documents, options);
    }

    public String getNamespace() {
        return mongoCollection.getNamespace().getFullName();
    }

    public MongoCollection<E> getMongoCollection() {
        return mongoCollection;
    }

    @Override
    public Collection<E> original() {
        return this;
    }
}
