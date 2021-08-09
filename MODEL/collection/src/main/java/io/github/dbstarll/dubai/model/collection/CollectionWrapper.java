package io.github.dbstarll.dubai.model.collection;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MapReduceIterable;
import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import io.github.dbstarll.dubai.model.entity.Entity;
import org.bson.conversions.Bson;

import java.util.List;

public abstract class CollectionWrapper<E extends Entity> extends AbstractCollection<E> {
    protected final Collection<E> collection;

    public CollectionWrapper(Collection<E> collection) {
        this.collection = collection;
    }

    public final Collection<E> getCollection() {
        return collection;
    }

    public Class<E> getEntityClass() {
        return collection.getEntityClass();
    }

    public long count(Bson filter, CountOptions options) {
        return collection.count(filter, options);
    }

    public <T> DistinctIterable<T> distinct(String fieldName, Bson filter, Class<T> resultClass) {
        return collection.distinct(fieldName, filter, resultClass);
    }

    public <T> FindIterable<T> find(Bson filter, Class<T> resultClass) {
        return collection.find(filter, resultClass);
    }

    public <T> AggregateIterable<T> aggregate(List<? extends Bson> pipeline, Class<T> resultClass) {
        return collection.aggregate(pipeline, resultClass);
    }

    public <T> MapReduceIterable<T> mapReduce(String mapFunction, String reduceFunction, Class<T> resultClass) {
        return collection.mapReduce(mapFunction, reduceFunction, resultClass);
    }

    public void insertOne(E document, InsertOneOptions options) {
        collection.insertOne(document, options);
    }

    public void insertMany(List<? extends E> documents, InsertManyOptions options) {
        collection.insertMany(documents, options);
    }

    public DeleteResult deleteOne(Bson filter, DeleteOptions options) {
        return collection.deleteOne(filter, options);
    }

    public DeleteResult deleteMany(Bson filter, DeleteOptions options) {
        return collection.deleteMany(filter, options);
    }

    public UpdateResult replaceOne(Bson filter, E replacement, ReplaceOptions replaceOptions) {
        return collection.replaceOne(filter, replacement, replaceOptions);
    }

    public UpdateResult updateOne(Bson filter, Bson update, UpdateOptions updateOptions) {
        return collection.updateOne(filter, update, updateOptions);
    }

    public UpdateResult updateMany(Bson filter, Bson update, UpdateOptions updateOptions) {
        return collection.updateMany(filter, update, updateOptions);
    }

    public E findOneAndDelete(Bson filter, FindOneAndDeleteOptions options) {
        return collection.findOneAndDelete(filter, options);
    }

    public E findOneAndReplace(Bson filter, E replacement, FindOneAndReplaceOptions options) {
        return collection.findOneAndReplace(filter, replacement, options);
    }

    public E findOneAndUpdate(Bson filter, Bson update, FindOneAndUpdateOptions options) {
        return collection.findOneAndUpdate(filter, update, options);
    }

    public Collection<E> original() {
        return collection.original();
    }
}
