package io.github.dbstarll.dubai.model.collection;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MapReduceIterable;
import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.EntityModifier;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

public abstract class AbstractCollection<E extends Entity> implements Collection<E> {

    @Override
    public final long count() {
        return count(new BsonDocument(), new CountOptions());
    }

    @Override
    public final long count(Bson filter) {
        return count(filter, new CountOptions());
    }

    @Override
    public final <T> DistinctIterable<T> distinct(String fieldName, Class<T> resultClass) {
        return distinct(fieldName, new BsonDocument(), resultClass);
    }

    @Override
    public final FindIterable<E> find() {
        return find(new BsonDocument(), this.getEntityClass());
    }

    @Override
    public final <T> FindIterable<T> find(Class<T> resultClass) {
        return find(new BsonDocument(), resultClass);
    }

    @Override
    public final FindIterable<E> find(Bson filter) {
        return find(filter, this.getEntityClass());
    }

    @Override
    public final AggregateIterable<E> aggregate(List<? extends Bson> pipeline) {
        return aggregate(pipeline, this.getEntityClass());
    }

    @Override
    public final MapReduceIterable<E> mapReduce(String mapFunction, String reduceFunction) {
        return mapReduce(mapFunction, reduceFunction, this.getEntityClass());
    }

    @Override
    public final void insertOne(E document) {
        insertOne(document, new InsertOneOptions());
    }

    @Override
    public final void insertMany(List<? extends E> documents) {
        insertMany(documents, new InsertManyOptions());
    }

    @Override
    public final DeleteResult deleteOne(Bson filter) {
        return deleteOne(filter, new DeleteOptions());
    }

    @Override
    public final DeleteResult deleteMany(Bson filter) {
        return deleteMany(filter, new DeleteOptions());
    }

    @Override
    public final UpdateResult replaceOne(Bson filter, E replacement) {
        return replaceOne(filter, replacement, new ReplaceOptions());
    }

    @Override
    public final UpdateResult updateOne(Bson filter, Bson update) {
        return updateOne(filter, update, new UpdateOptions());
    }

    @Override
    public final UpdateResult updateMany(Bson filter, Bson update) {
        return updateMany(filter, update, new UpdateOptions());
    }

    @Override
    public final E findOneAndDelete(Bson filter) {
        return findOneAndDelete(filter, new FindOneAndDeleteOptions());
    }

    @Override
    public final E findOneAndReplace(Bson filter, E replacement) {
        return findOneAndReplace(filter, replacement, new FindOneAndReplaceOptions());
    }

    @Override
    public final E findOneAndUpdate(Bson filter, Bson update) {
        return findOneAndUpdate(filter, update, new FindOneAndUpdateOptions());
    }

    @Override
    public final E findOne() {
        return findOne(new BsonDocument());
    }

    @Override
    public E findOne(Bson filter) {
        return find(filter).limit(1).first();
    }

    @Override
    public final E findById(ObjectId id) {
        if (id == null) {
            return null;
        }
        return findOne(Filters.eq(Entity.FIELD_NAME_ID, id));
    }

    @Override
    public final E updateById(ObjectId id, Bson update) {
        return updateById(id, update, new FindOneAndUpdateOptions());
    }

    @Override
    public final E updateById(ObjectId id, Bson update, FindOneAndUpdateOptions updateOptions) {
        if (id == null) {
            return null;
        }

        return findOneAndUpdate(Filters.eq(Entity.FIELD_NAME_ID, id), update, updateOptions);
    }

    @Override
    public final E deleteById(ObjectId id) {
        return deleteById(id, new FindOneAndDeleteOptions());
    }

    @Override
    public final E deleteById(ObjectId id, FindOneAndDeleteOptions options) {
        if (id == null) {
            return null;
        }

        return findOneAndDelete(Filters.eq(Entity.FIELD_NAME_ID, id), options);
    }

    @Override
    public final E save(E entity) {
        return save(entity, null);
    }

    @Override
    public final E save(E entity, ObjectId newEntityId) {
        if (entity == null) {
            return null;
        }

        final Date now = new Date();
        setEntityLastModified(entity, now);

        if (entity.getId() == null) {
            setEntityId(entity, newEntityId == null ? new ObjectId() : newEntityId);
            setEntityDateCreated(entity, newEntityId == null ? now : newEntityId.getDate());

            this.insertOne(entity);
        } else {
            this.replaceOne(Filters.eq(Entity.FIELD_NAME_ID, entity.getId()), entity);
        }

        return entity;
    }

    @Override
    public final FindIterable<E> findByIds(java.util.Collection<ObjectId> ids) {
        return find(Filters.in(Entity.FIELD_NAME_ID, ids));
    }

    @Override
    public final boolean contains(ObjectId id) {
        return count(Filters.eq(id)) > 0;
    }

    /**
     * 设置实体ID.
     *
     * @param entity 被设置的实体
     * @param id     被设置的ID
     */
    private static final <E extends Entity> void setEntityId(E entity, ObjectId id) {
        ((EntityModifier) entity).setId(id);
    }

    /**
     * 设置实体的创建时间.
     *
     * @param entity      被设置的实体
     * @param dateCreated 被设置的创建时间
     */
    private static final <E extends Entity> void setEntityDateCreated(E entity, Date dateCreated) {
        ((EntityModifier) entity).setDateCreated(dateCreated);
    }

    /**
     * 设置实体的最后修改时间.
     *
     * @param entity       被设置的实体
     * @param lastModified 被设置的最后修改时间
     */
    private static final <E extends Entity> void setEntityLastModified(E entity, Date lastModified) {
        if (EntityModifier.class.isInstance(entity)) {
            ((EntityModifier) entity).setLastModified(lastModified);
        } else {
            throw new IllegalArgumentException("UnModify entity: " + entity.getClass().getName());
        }
    }
}
