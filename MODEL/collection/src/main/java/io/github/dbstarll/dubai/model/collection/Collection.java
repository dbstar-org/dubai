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
import org.bson.types.ObjectId;

import java.util.List;

public interface Collection<E extends Entity> {
    /**
     * Get the class of documents stored in this collection.
     *
     * @return the class
     */
    Class<E> getEntityClass();

    /**
     * Counts the number of documents in the collection.
     *
     * @return the number of documents in the collection
     */
    long count();

    /**
     * Counts the number of documents in the collection according to the given options.
     *
     * @param filter the query filter
     * @return the number of documents in the collection
     */
    long count(Bson filter);

    /**
     * Counts the number of documents in the collection according to the given options.
     *
     * @param filter  the query filter
     * @param options the options describing the count
     * @return the number of documents in the collection
     */
    long count(Bson filter, CountOptions options);

    /**
     * Gets the distinct values of the specified field name.
     *
     * @param fieldName   the field name
     * @param resultClass the class to cast any distinct items into.
     * @param <T>         the target type of the iterable.
     * @return an iterable of distinct values
     */
    <T> DistinctIterable<T> distinct(String fieldName, Class<T> resultClass);

    /**
     * Gets the distinct values of the specified field name.
     *
     * @param fieldName   the field name
     * @param filter      the query filter
     * @param resultClass the class to cast any distinct items into.
     * @param <T>         the target type of the iterable.
     * @return an iterable of distinct values
     */
    <T> DistinctIterable<T> distinct(String fieldName, Bson filter, Class<T> resultClass);

    /**
     * Finds all documents in the collection.
     *
     * @return the find iterable interface
     */
    FindIterable<E> find();

    /**
     * Finds all documents in the collection.
     *
     * @param resultClass the class to decode each document into
     * @param <T>         the target document type of the iterable.
     * @return the find iterable interface
     */
    <T> FindIterable<T> find(Class<T> resultClass);

    /**
     * Finds all documents in the collection.
     *
     * @param filter the query filter
     * @return the find iterable interface
     */
    FindIterable<E> find(Bson filter);

    /**
     * Finds all documents in the collection.
     *
     * @param filter      the query filter
     * @param resultClass the class to decode each document into
     * @param <T>         the target document type of the iterable.
     * @return the find iterable interface
     */
    <T> FindIterable<T> find(Bson filter, Class<T> resultClass);

    /**
     * Aggregates documents according to the specified aggregation pipeline.
     *
     * @param pipeline the aggregate pipeline
     * @return an iterable containing the result of the aggregation operation
     */
    AggregateIterable<E> aggregate(List<? extends Bson> pipeline);

    /**
     * Aggregates documents according to the specified aggregation pipeline.
     *
     * @param pipeline    the aggregate pipeline
     * @param resultClass the class to decode each document into
     * @param <T>         the target document type of the iterable.
     * @return an iterable containing the result of the aggregation operation
     */
    <T> AggregateIterable<T> aggregate(List<? extends Bson> pipeline, Class<T> resultClass);

    /**
     * Aggregates documents according to the specified map-reduce function.
     *
     * @param mapFunction    A JavaScript function that associates or "maps" a value with a key and emits
     *                       the key and value pair.
     * @param reduceFunction A JavaScript function that "reduces" to a single object all the values
     *                       associated with a particular key.
     * @return an iterable containing the result of the map-reduce operation
     */
    MapReduceIterable<E> mapReduce(String mapFunction, String reduceFunction);

    /**
     * Aggregates documents according to the specified map-reduce function.
     *
     * @param mapFunction    A JavaScript function that associates or "maps" a value with a key and emits
     *                       the key and value pair.
     * @param reduceFunction A JavaScript function that "reduces" to a single object all the values
     *                       associated with a particular key.
     * @param resultClass    the class to decode each resulting document into.
     * @param <T>            the target document type of the iterable.
     * @return an iterable containing the result of the map-reduce operation
     */
    <T> MapReduceIterable<T> mapReduce(String mapFunction, String reduceFunction, Class<T> resultClass);

    /**
     * Inserts the provided document. If the document is missing an identifier, the driver should
     * generate one.
     *
     * @param document the document to insert
     * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to
     *                                                the insert command
     * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil
     *                                                the write concern
     * @throws com.mongodb.MongoException             if the write failed due some other failure
     */
    void insertOne(E document);

    /**
     * Inserts the provided document. If the document is missing an identifier, the driver should
     * generate one.
     *
     * @param document the document to insert
     * @param options  the options to apply to the operation
     * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to
     *                                                the insert command
     * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil
     *                                                the write concern
     * @throws com.mongodb.MongoCommandException      if the write failed due to document validation
     *                                                reasons
     * @throws com.mongodb.MongoException             if the write failed due some other failure
     * @since 3.2
     */
    void insertOne(E document, InsertOneOptions options);

    /**
     * Inserts one or more documents. A call to this method is equivalent to a call to the
     * {@code bulkWrite} method
     *
     * @param documents the documents to insert
     * @throws com.mongodb.MongoBulkWriteException if there's an exception in the bulk write operation
     * @throws com.mongodb.MongoException          if the write failed due some other failure
     * @see com.mongodb.client.MongoCollection#bulkWrite
     */
    void insertMany(List<? extends E> documents);

    /**
     * Inserts one or more documents. A call to this method is equivalent to a call to the
     * {@code bulkWrite} method
     *
     * @param documents the documents to insert
     * @param options   the options to apply to the operation
     * @throws com.mongodb.DuplicateKeyException if the write failed to a duplicate unique key
     * @throws com.mongodb.WriteConcernException if the write failed due being unable to fulfil the
     *                                           write concern
     * @throws com.mongodb.MongoException        if the write failed due some other failure
     */
    void insertMany(List<? extends E> documents, InsertManyOptions options);

    /**
     * Removes at most one document from the collection that matches the given filter. If no documents
     * match, the collection is not modified.
     *
     * @param filter the query filter to apply the the delete operation
     * @return the result of the remove one operation
     * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to
     *                                                the delete command
     * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil
     *                                                the write concern
     * @throws com.mongodb.MongoException             if the write failed due some other failure
     */
    DeleteResult deleteOne(Bson filter);

    /**
     * Removes at most one document from the collection that matches the given filter. If no documents
     * match, the collection is not modified.
     *
     * @param filter  the query filter to apply the the delete operation
     * @param options the options to apply to the delete operation
     * @return the result of the remove one operation
     * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to
     *                                                the delete command
     * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil
     *                                                the write concern
     * @throws com.mongodb.MongoException             if the write failed due some other failure
     * @since 3.4
     */
    DeleteResult deleteOne(Bson filter, DeleteOptions options);

    /**
     * Removes all documents from the collection that match the given query filter. If no documents
     * match, the collection is not modified.
     *
     * @param filter the query filter to apply the the delete operation
     * @return the result of the remove many operation
     * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to
     *                                                the delete command
     * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil
     *                                                the write concern
     * @throws com.mongodb.MongoException             if the write failed due some other failure
     */
    DeleteResult deleteMany(Bson filter);

    /**
     * Removes all documents from the collection that match the given query filter. If no documents
     * match, the collection is not modified.
     *
     * @param filter  the query filter to apply the the delete operation
     * @param options the options to apply to the delete operation
     * @return the result of the remove many operation
     * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to
     *                                                the delete command
     * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil
     *                                                the write concern
     * @throws com.mongodb.MongoException             if the write failed due some other failure
     * @since 3.4
     */
    DeleteResult deleteMany(Bson filter, DeleteOptions options);

    /**
     * Replace a document in the collection according to the specified arguments.
     *
     * @param filter      the query filter to apply the the replace operation
     * @param replacement the replacement document
     * @return the result of the replace one operation
     * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to
     *                                                the replace command
     * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil
     *                                                the write concern
     * @throws com.mongodb.MongoException             if the write failed due some other failure
     */
    UpdateResult replaceOne(Bson filter, E replacement);

    /**
     * Replace a document in the collection according to the specified arguments.
     *
     * @param filter         the query filter to apply the the replace operation
     * @param replacement    the replacement document
     * @param replaceOptions the options to apply to the replace operation
     * @return the result of the replace one operation
     * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to
     *                                                the replace command
     * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil
     *                                                the write concern
     * @throws com.mongodb.MongoException             if the write failed due some other failure
     */
    UpdateResult replaceOne(Bson filter, E replacement, ReplaceOptions replaceOptions);

    /**
     * Update a single document in the collection according to the specified arguments.
     *
     * @param filter a document describing the query filter, which may not be null.
     * @param update a document describing the update, which may not be null. The update to apply must
     *               include only update operators.
     * @return the result of the update one operation
     * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to
     *                                                the update command
     * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil
     *                                                the write concern
     * @throws com.mongodb.MongoException             if the write failed due some other failure
     */
    UpdateResult updateOne(Bson filter, Bson update);

    /**
     * Update a single document in the collection according to the specified arguments.
     *
     * @param filter        a document describing the query filter, which may not be null.
     * @param update        a document describing the update, which may not be null. The update to apply must
     *                      include only update operators.
     * @param updateOptions the options to apply to the update operation
     * @return the result of the update one operation
     * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to
     *                                                the update command
     * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil
     *                                                the write concern
     * @throws com.mongodb.MongoException             if the write failed due some other failure
     */
    UpdateResult updateOne(Bson filter, Bson update, UpdateOptions updateOptions);

    /**
     * Update all documents in the collection according to the specified arguments.
     *
     * @param filter a document describing the query filter, which may not be null.
     * @param update a document describing the update, which may not be null. The update to apply must
     *               include only update operators.
     * @return the result of the update many operation
     * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to
     *                                                the update command
     * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil
     *                                                the write concern
     * @throws com.mongodb.MongoException             if the write failed due some other failure
     */
    UpdateResult updateMany(Bson filter, Bson update);

    /**
     * Update all documents in the collection according to the specified arguments.
     *
     * @param filter        a document describing the query filter, which may not be null.
     * @param update        a document describing the update, which may not be null. The update to apply must
     *                      include only update operators.
     * @param updateOptions the options to apply to the update operation
     * @return the result of the update many operation
     * @throws com.mongodb.MongoWriteException        if the write failed due some other failure specific to
     *                                                the update command
     * @throws com.mongodb.MongoWriteConcernException if the write failed due being unable to fulfil
     *                                                the write concern
     * @throws com.mongodb.MongoException             if the write failed due some other failure
     */
    UpdateResult updateMany(Bson filter, Bson update, UpdateOptions updateOptions);

    /**
     * Atomically find a document and remove it.
     *
     * @param filter the query filter to find the document with
     * @return the document that was removed. If no documents matched the query filter, then null will
     * be returned
     */
    E findOneAndDelete(Bson filter);

    /**
     * Atomically find a document and remove it.
     *
     * @param filter  the query filter to find the document with
     * @param options the options to apply to the operation
     * @return the document that was removed. If no documents matched the query filter, then null will
     * be returned
     */
    E findOneAndDelete(Bson filter, FindOneAndDeleteOptions options);

    /**
     * Atomically find a document and replace it.
     *
     * @param filter      the query filter to apply the the replace operation
     * @param replacement the replacement document
     * @return the document that was replaced. Depending on the value of the {@code returnOriginal}
     * property, this will either be the document as it was before the update or as it is
     * after the update. If no documents matched the query filter, then null will be returned
     */
    E findOneAndReplace(Bson filter, E replacement);

    /**
     * Atomically find a document and replace it.
     *
     * @param filter      the query filter to apply the the replace operation
     * @param replacement the replacement document
     * @param options     the options to apply to the operation
     * @return the document that was replaced. Depending on the value of the {@code returnOriginal}
     * property, this will either be the document as it was before the update or as it is
     * after the update. If no documents matched the query filter, then null will be returned
     */
    E findOneAndReplace(Bson filter, E replacement, FindOneAndReplaceOptions options);

    /**
     * Atomically find a document and update it.
     *
     * @param filter a document describing the query filter, which may not be null.
     * @param update a document describing the update, which may not be null. The update to apply must
     *               include only update operators.
     * @return the document that was updated before the update was applied. If no documents matched
     * the query filter, then null will be returned
     */
    E findOneAndUpdate(Bson filter, Bson update);

    /**
     * Atomically find a document and update it.
     *
     * @param filter  a document describing the query filter, which may not be null.
     * @param update  a document describing the update, which may not be null. The update to apply must
     *                include only update operators.
     * @param options the options to apply to the operation
     * @return the document that was updated. Depending on the value of the {@code returnOriginal}
     * property, this will either be the document as it was before the update or as it is
     * after the update. If no documents matched the query filter, then null will be returned
     */
    E findOneAndUpdate(Bson filter, Bson update, FindOneAndUpdateOptions options);

    E findOne();

    E findOne(Bson filter);

    E findById(ObjectId id);

    FindIterable<E> findByIds(java.util.Collection<ObjectId> ids);

    E updateById(ObjectId id, Bson update);

    E updateById(ObjectId id, Bson update, FindOneAndUpdateOptions updateOptions);

    E deleteById(ObjectId id);

    E deleteById(ObjectId id, FindOneAndDeleteOptions options);

    /**
     * 插入或更新一个实体.
     *
     * <p>
     * 在entity的id不为null时，执行更新操作，将lastModified更新为当前时间。
     * 在entity的id为null时，执行插入操作，生成一个新的id，并且则设置entity的dateCreated为当前时间。
     * </p>
     *
     * @param entity 需要插入或更新的实体
     * @return 返回更新后的实体
     */
    E save(E entity);

    /**
     * 插入或更新一个实体.
     *
     * <p>
     * 在entity的id不为null时，执行更新操作，将lastModified更新为当前时间。 在entity的id为null时，执行插入操作：
     * 这时如果传入的newEntityId也为null，则生成一个新的id，否则使用传入的newEntityId。
     * </p>
     *
     * @param entity      需要插入或更新的实体
     * @param newEntityId 插入时指定entity的id
     * @return 返回更新后的实体
     */
    E save(E entity, ObjectId newEntityId);

    /**
     * 返回不带defunct字段过滤的Collection对象，以便进行物理删除和全量查询.
     *
     * @return
     */
    Collection<E> original();

    /**
     * 检查仓库中是否包含指定id的entity.
     *
     * @param id id of entity
     * @return 仓库中是否包含指定id的entity
     */
    boolean contains(ObjectId id);
}
