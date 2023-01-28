package io.github.dbstarll.dubai.model.collection;

import com.mongodb.client.DistinctIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.CountOptions;
import com.mongodb.client.model.DeleteOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndDeleteOptions;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.func.Defunctable;
import org.bson.BsonBoolean;
import org.bson.conversions.Bson;

import java.util.concurrent.TimeUnit;

public class DefunctableCollection<E extends Entity> extends CollectionWrapper<E> {
    public static final Bson DEFUNCT = Updates.set(Defunctable.FIELD_NAME_DEFUNCT, BsonBoolean.TRUE);

    /**
     * 构造一个带伪删除功能的集合.
     *
     * @param collection 被封装的集合
     */
    public DefunctableCollection(final Collection<E> collection) {
        super(collection);
    }

    @Override
    public Collection<E> original() {
        return getCollection();
    }

    /**
     * 在原查询条件上封装一层伪删除的查询条件.
     *
     * @param filter 原查询条件
     * @return 封装后的查询条件
     */
    protected Bson queryFilter(final Bson filter) {
        final Bson filterDefunct = Filters.eq(Defunctable.FIELD_NAME_DEFUNCT, BsonBoolean.FALSE);
        if (filter == null) {
            return filterDefunct;
        } else {
            return Filters.and(filter, filterDefunct);
        }
    }

    @Override
    public UpdateResult replaceOne(final Bson filter, final E replacement, final ReplaceOptions replaceOptions) {
        return super.replaceOne(queryFilter(filter), replacement, replaceOptions);
    }

    @Override
    public UpdateResult updateOne(final Bson filter, final Bson update, final UpdateOptions updateOptions) {
        return super.updateOne(queryFilter(filter), update, updateOptions);
    }

    @Override
    public UpdateResult updateMany(final Bson filter, final Bson update, final UpdateOptions updateOptions) {
        return super.updateMany(queryFilter(filter), update, updateOptions);
    }

    @Override
    public long count(final Bson filter, final CountOptions options) {
        return super.count(queryFilter(filter), options);
    }

    @Override
    public <T> DistinctIterable<T> distinct(final String fieldName, final Bson filter, final Class<T> resultClass) {
        return super.distinct(fieldName, queryFilter(filter), resultClass);
    }

    @Override
    public <T> FindIterable<T> find(final Bson filter, final Class<T> resultClass) {
        return super.find(queryFilter(filter), resultClass);
    }

    @Override
    public DeleteResult deleteOne(final Bson filter, final DeleteOptions options) {
        return DeleteResult.acknowledged(
                updateOne(filter, DEFUNCT, new UpdateOptions().collation(options.getCollation())).getModifiedCount());
    }

    @Override
    public DeleteResult deleteMany(final Bson filter, final DeleteOptions options) {
        return DeleteResult.acknowledged(
                updateMany(filter, DEFUNCT, new UpdateOptions().collation(options.getCollation())).getModifiedCount());
    }

    @Override
    public E findOneAndDelete(final Bson filter, final FindOneAndDeleteOptions options) {
        FindOneAndUpdateOptions updateOptions = new FindOneAndUpdateOptions();
        updateOptions.collation(options.getCollation()).projection(options.getProjection()).sort(options.getSort())
                .maxTime(options.getMaxTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
        return super.findOneAndUpdate(queryFilter(filter), DEFUNCT, updateOptions);
    }

    @Override
    public E findOneAndReplace(final Bson filter, final E replacement, final FindOneAndReplaceOptions options) {
        return super.findOneAndReplace(queryFilter(filter), replacement, options);
    }

    @Override
    public E findOneAndUpdate(final Bson filter, final Bson update, final FindOneAndUpdateOptions options) {
        return super.findOneAndUpdate(queryFilter(filter), update, options);
    }

    @Override
    public E findOne(final Bson filter) {
        return collection.findOne(queryFilter(filter));
    }
}
