package io.github.dbstarll.dubai.model.collection;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.CountOptions;
import com.mongodb.client.model.DeleteOptions;
import com.mongodb.client.model.FindOneAndDeleteOptions;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.InsertManyOptions;
import com.mongodb.client.model.InsertOneOptions;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import io.github.dbstarll.dubai.model.entity.Entity;
import org.bson.conversions.Bson;

import java.util.List;

public abstract class CollectionWrapper<E extends Entity> extends AbstractCollection<E> {
    protected final Collection<E> collection;

    /**
     * 构造一个Collection的封装类.
     *
     * @param collection 被封装的Collection
     */
    protected CollectionWrapper(final Collection<E> collection) {
        this.collection = collection;
    }

    /**
     * 获得被封装的Collection.
     *
     * @return 被封装的Collection
     */
    public final Collection<E> getCollection() {
        return collection;
    }

    @Override
    public Class<E> getEntityClass() {
        return collection.getEntityClass();
    }

    @Override
    public long count(final Bson filter, final CountOptions options) {
        return collection.count(filter, options);
    }

    @Override
    public <T> DistinctIterable<T> distinct(final String fieldName, final Bson filter, final Class<T> resultClass) {
        return collection.distinct(fieldName, filter, resultClass);
    }

    @Override
    public <T> FindIterable<T> find(final Bson filter, final Class<T> resultClass) {
        return collection.find(filter, resultClass);
    }

    @Override
    public <T> AggregateIterable<T> aggregate(final List<? extends Bson> pipeline, final Class<T> resultClass) {
        return collection.aggregate(pipeline, resultClass);
    }

    @Override
    public void insertOne(final E document, final InsertOneOptions options) {
        collection.insertOne(document, options);
    }

    @Override
    public void insertMany(final List<? extends E> documents, final InsertManyOptions options) {
        collection.insertMany(documents, options);
    }

    @Override
    public DeleteResult deleteOne(final Bson filter, final DeleteOptions options) {
        return collection.deleteOne(filter, options);
    }

    @Override
    public DeleteResult deleteMany(final Bson filter, final DeleteOptions options) {
        return collection.deleteMany(filter, options);
    }

    @Override
    public UpdateResult replaceOne(final Bson filter, final E replacement, final ReplaceOptions replaceOptions) {
        return collection.replaceOne(filter, replacement, replaceOptions);
    }

    @Override
    public UpdateResult updateOne(final Bson filter, final Bson update, final UpdateOptions updateOptions) {
        return collection.updateOne(filter, update, updateOptions);
    }

    @Override
    public UpdateResult updateMany(final Bson filter, final Bson update, final UpdateOptions updateOptions) {
        return collection.updateMany(filter, update, updateOptions);
    }

    @Override
    public E findOneAndDelete(final Bson filter, final FindOneAndDeleteOptions options) {
        return collection.findOneAndDelete(filter, options);
    }

    @Override
    public E findOneAndReplace(final Bson filter, final E replacement, final FindOneAndReplaceOptions options) {
        return collection.findOneAndReplace(filter, replacement, options);
    }

    @Override
    public E findOneAndUpdate(final Bson filter, final Bson update, final FindOneAndUpdateOptions options) {
        return collection.findOneAndUpdate(filter, update, options);
    }

    @Override
    public Collection<E> original() {
        return collection.original();
    }
}
