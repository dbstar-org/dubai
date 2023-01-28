package io.github.dbstarll.dubai.model.collection;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
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
import org.bson.BsonDocument;
import org.bson.conversions.Bson;

import java.util.List;

public class BaseCollection<E extends Entity> extends AbstractCollection<E> {
    private final MongoCollection<E> mongoCollection;

    /**
     * 构建BaseCollection.
     *
     * @param mongoCollection MongoCollection
     */
    public BaseCollection(final MongoCollection<E> mongoCollection) {
        this.mongoCollection = mongoCollection;
    }

    /**
     * 将Bson包装成查询用的BsonDocument.
     *
     * @param filter 查询过滤器
     * @return 包装后的BsonDocument
     */
    protected Bson queryFilter(final Bson filter) {
        if (null == filter) {
            return new BsonDocument();
        } else {
            return filter.toBsonDocument(getEntityClass(), mongoCollection.getCodecRegistry());
        }
    }

    @Override
    public long count(final Bson filter, final CountOptions options) {
        return mongoCollection.countDocuments(queryFilter(filter), options);
    }

    @Override
    public <T> DistinctIterable<T> distinct(final String fieldName, final Bson filter, final Class<T> resultClass) {
        return mongoCollection.distinct(fieldName, queryFilter(filter), resultClass);
    }

    @Override
    public <T> FindIterable<T> find(final Bson filter, final Class<T> resultClass) {
        return mongoCollection.find(queryFilter(filter), resultClass);
    }

    @Override
    public DeleteResult deleteOne(final Bson filter, final DeleteOptions options) {
        return mongoCollection.deleteOne(filter, options);
    }

    @Override
    public DeleteResult deleteMany(final Bson filter, final DeleteOptions options) {
        return mongoCollection.deleteMany(filter, options);
    }

    @Override
    public UpdateResult replaceOne(final Bson filter, final E replacement, final ReplaceOptions replaceOptions) {
        return mongoCollection.replaceOne(queryFilter(filter), replacement, replaceOptions);
    }

    @Override
    public UpdateResult updateOne(final Bson filter, final Bson update, final UpdateOptions updateOptions) {
        return mongoCollection.updateOne(queryFilter(filter), update, updateOptions);
    }

    @Override
    public UpdateResult updateMany(final Bson filter, final Bson update, final UpdateOptions updateOptions) {
        return mongoCollection.updateMany(queryFilter(filter), update, updateOptions);
    }

    @Override
    public E findOneAndDelete(final Bson filter, final FindOneAndDeleteOptions options) {
        return mongoCollection.findOneAndDelete(queryFilter(filter), options);
    }

    @Override
    public E findOneAndReplace(final Bson filter, final E replacement, final FindOneAndReplaceOptions options) {
        return mongoCollection.findOneAndReplace(queryFilter(filter), replacement, options);
    }

    @Override
    public E findOneAndUpdate(final Bson filter, final Bson update, final FindOneAndUpdateOptions options) {
        return mongoCollection.findOneAndUpdate(queryFilter(filter), update, options);
    }

    @Override
    public Class<E> getEntityClass() {
        return mongoCollection.getDocumentClass();
    }

    @Override
    public <T> AggregateIterable<T> aggregate(final List<? extends Bson> pipeline, final Class<T> resultClass) {
        return mongoCollection.aggregate(pipeline, resultClass);
    }

    @Override
    public void insertOne(final E document, final InsertOneOptions options) {
        mongoCollection.insertOne(document, options);
    }

    @Override
    public void insertMany(final List<? extends E> documents, final InsertManyOptions options) {
        mongoCollection.insertMany(documents, options);
    }

    /**
     * 获得namespace信息.
     *
     * @return namespace
     */
    public final String getNamespace() {
        return mongoCollection.getNamespace().getFullName();
    }

    /**
     * 获得MongoCollection实例.
     *
     * @return MongoCollection
     */
    public final MongoCollection<E> getMongoCollection() {
        return mongoCollection;
    }

    @Override
    public Collection<E> original() {
        return this;
    }
}
