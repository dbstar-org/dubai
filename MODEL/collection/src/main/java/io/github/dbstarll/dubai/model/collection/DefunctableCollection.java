package io.github.dbstarll.dubai.model.collection;

import com.mongodb.client.DistinctIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.func.Defunctable;
import org.bson.BsonBoolean;
import org.bson.conversions.Bson;

import java.util.concurrent.TimeUnit;

public class DefunctableCollection<E extends Entity> extends CollectionWrapper<E> {
    public static final Bson DEFUNCT = Updates.set(Defunctable.FIELD_NAME_DEFUNCT, BsonBoolean.TRUE);

    public DefunctableCollection(Collection<E> collection) {
        super(collection);
    }

    @Override
    public Collection<E> original() {
        return getCollection();
    }

    protected Bson queryFilter(Bson filter) {
        final Bson filterDefunct = Filters.eq(Defunctable.FIELD_NAME_DEFUNCT, BsonBoolean.FALSE);
        if (filter == null) {
            return filterDefunct;
        } else {
            return Filters.and(filter, filterDefunct);
        }
    }

    @Override
    public UpdateResult replaceOne(Bson filter, E replacement, ReplaceOptions replaceOptions) {
        return super.replaceOne(queryFilter(filter), replacement, replaceOptions);
    }

    @Override
    public UpdateResult updateOne(Bson filter, Bson update, UpdateOptions updateOptions) {
        return super.updateOne(queryFilter(filter), update, updateOptions);
    }

    @Override
    public UpdateResult updateMany(Bson filter, Bson update, UpdateOptions updateOptions) {
        return super.updateMany(queryFilter(filter), update, updateOptions);
    }

    @Override
    public long count(Bson filter, CountOptions options) {
        return super.count(queryFilter(filter), options);
    }

    @Override
    public <T> DistinctIterable<T> distinct(String fieldName, Bson filter, Class<T> resultClass) {
        return super.distinct(fieldName, queryFilter(filter), resultClass);
    }

    @Override
    public <T> FindIterable<T> find(Bson filter, Class<T> resultClass) {
        return super.find(queryFilter(filter), resultClass);
    }

    @Override
    public DeleteResult deleteOne(Bson filter, DeleteOptions options) {
        return DeleteResult.acknowledged(
                updateOne(filter, DEFUNCT, new UpdateOptions().collation(options.getCollation())).getModifiedCount());
    }

    @Override
    public DeleteResult deleteMany(Bson filter, DeleteOptions options) {
        return DeleteResult.acknowledged(
                updateMany(filter, DEFUNCT, new UpdateOptions().collation(options.getCollation())).getModifiedCount());
    }

    @Override
    public E findOneAndDelete(Bson filter, FindOneAndDeleteOptions options) {
        FindOneAndUpdateOptions updateOptions = new FindOneAndUpdateOptions();
        updateOptions.collation(options.getCollation()).projection(options.getProjection()).sort(options.getSort())
                .maxTime(options.getMaxTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
        return super.findOneAndUpdate(queryFilter(filter), DEFUNCT, updateOptions);
    }

    @Override
    public E findOneAndReplace(Bson filter, E replacement, FindOneAndReplaceOptions options) {
        return super.findOneAndReplace(queryFilter(filter), replacement, options);
    }

    @Override
    public E findOneAndUpdate(Bson filter, Bson update, FindOneAndUpdateOptions options) {
        return super.findOneAndUpdate(queryFilter(filter), update, options);
    }

    @Override
    public E findOne(Bson filter) {
        return collection.findOne(queryFilter(filter));
    }
}
