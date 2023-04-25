package io.github.dbstarll.dubai.model.service;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.BsonField;
import com.mongodb.client.model.Variable;
import com.mongodb.lang.Nullable;
import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.utils.lang.wrapper.EntryWrapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.bson.BsonDocument;
import org.bson.codecs.DecoderContext;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

public final class Aggregator<E extends Entity, S extends Service<E>> {
    private final S service;
    private final Collection<E> collection;
    private final List<Bson> pipelines;
    private final Map<String, Service<? extends Entity>> asMap;

    private Aggregator(final S service, final Collection<E> collection) {
        this.service = service;
        this.collection = collection;
        this.pipelines = new LinkedList<>();
        this.asMap = new HashMap<>();
    }

    /**
     * 每个left join的Service只取第一个匹配实体的聚合结果.
     *
     * @param decoderContext the decoder context
     * @return an iterable containing the result of the aggregation operation
     */
    public MongoIterable<Entry<E, Map<Class<? extends Entity>, Entity>>> joinOne(final DecoderContext decoderContext) {
        return aggregate(BsonDocument.class).map(t -> {
            final E entity = helper(service).decode(t.asBsonReader(), decoderContext);
            final Map<Class<? extends Entity>, Entity> joins = asMap.entrySet().stream().map(e -> {
                final Service<? extends Entity> joinService = e.getValue();
                return EntryWrapper.wrap(joinService.getEntityClass(),
                        helper(joinService).decodeOne(t.getArray(e.getKey()), decoderContext));
            }).collect((Supplier<Map<Class<? extends Entity>, Entity>>) HashMap::new,
                    (map, entry) -> map.computeIfAbsent(entry.getKey(), k -> entry.getValue()),
                    Map::putAll);
            return EntryWrapper.wrap(entity, joins);
        });
    }

    /**
     * 每个left join的Service取所有匹配实体的聚合结果.
     *
     * @param decoderContext the decoder context
     * @return an iterable containing the result of the aggregation operation
     */
    public MongoIterable<Entry<E, Map<Class<? extends Entity>, List<Entity>>>> join(
            final DecoderContext decoderContext) {
        return aggregate(BsonDocument.class).map(t -> {
            final E entity = helper(service).decode(t.asBsonReader(), decoderContext);
            final Map<Class<? extends Entity>, List<Entity>> joins = asMap.entrySet().stream().map(e -> {
                final Service<? extends Entity> joinService = e.getValue();
                return EntryWrapper.wrap(joinService.getEntityClass(),
                        helper(joinService).decode(t.getArray(e.getKey()), decoderContext));
            }).collect((Supplier<Map<Class<? extends Entity>, List<Entity>>>) HashMap::new,
                    (map, entry) -> map.computeIfAbsent(entry.getKey(), k -> new ArrayList<>(entry.getValue())),
                    Map::putAll);
            return EntryWrapper.wrap(entity, joins);
        });
    }

    /**
     * Aggregates documents according to the specified aggregation pipeline.
     *
     * @param resultClass the class to decode each document into
     * @param <T>         the target document type of the iterable.
     * @return an iterable containing the result of the aggregation operation
     */
    public <T> AggregateIterable<T> aggregate(final Class<T> resultClass) {
        return collection.aggregate(pipelines, resultClass);
    }

    /**
     * 构造一个Builder实例，用于继续配置聚合查询.
     *
     * @param service    服务类
     * @param collection 集合类
     * @param <E>        实体类
     * @param <S>        服务类
     * @return Builder实例
     */
    public static <E extends Entity, S extends Service<E>> Builder<E, S> builder(
            final S service, final Collection<E> collection) {
        return new Builder<>(service, collection);
    }

    public static final class Builder<E extends Entity, S extends Service<E>> {
        private final Aggregator<E, S> aggregator;

        private Builder(final S service, final Collection<E> collection) {
            this.aggregator = new Aggregator<>(service, collection);
        }

        /**
         * Creates a $match pipeline stage for the specified filter.
         *
         * @param filter the filter to match
         * @return Builder self
         */
        public Builder<E, S> match(final Bson filter) {
            if (filter != null) {
                aggregator.pipelines.add(Aggregates.match(filter));
            }
            return this;
        }

        /**
         * Creates a $sort pipeline stage for the specified sort specification.
         *
         * @param sort the sort specification
         * @return Builder self
         */
        public Builder<E, S> sort(final Bson sort) {
            if (sort != null) {
                aggregator.pipelines.add(Aggregates.sort(sort));
            }
            return this;
        }

        /**
         * Creates a $skip pipeline stage.
         *
         * @param skip the number of documents to skip
         * @return Builder self
         */
        public Builder<E, S> skip(final int skip) {
            if (skip > 0) {
                aggregator.pipelines.add(Aggregates.skip(skip));
            }
            return this;
        }

        /**
         * Creates a $limit pipeline stage for the specified filter.
         *
         * @param limit the limit
         * @return Builder self
         */
        public Builder<E, S> limit(final int limit) {
            if (limit > 0) {
                aggregator.pipelines.add(Aggregates.limit(limit));
            }
            return this;
        }

        /**
         * Creates a $project pipeline stage for the specified projection.
         *
         * @param projection the projection
         * @return Builder self
         */
        public Builder<E, S> project(final Bson projection) {
            if (projection != null) {
                aggregator.pipelines.add(Aggregates.project(projection));
            }
            return this;
        }

        /**
         * Creates a $group pipeline stage for the specified filter.
         *
         * @param <TExpression>     the expression type
         * @param id                the id expression for the group, which may be null
         * @param fieldAccumulators zero or more field accumulator pairs
         * @return Builder self
         */
        public <TExpression> Builder<E, S> group(@Nullable final TExpression id, final BsonField... fieldAccumulators) {
            aggregator.pipelines.add(Aggregates.group(id, fieldAccumulators));
            return this;
        }

        /**
         * Creates a $sample pipeline stage with the specified sample size.
         *
         * @param size the sample size
         * @return Builder self
         */
        public Builder<E, S> sample(final int size) {
            aggregator.pipelines.add(Aggregates.sample(size));
            return this;
        }

        /**
         * Creates a $lookup pipeline stage, joining the current collection with the one specified in from
         * using equality match between the local field and the foreign _id field.
         *
         * @param joinService join的服务类
         * @param localField  the field from the local collection to match values against.
         * @param <E1>        join的实体类
         * @param <S1>        join的服务类
         * @return Builder self
         */
        public <E1 extends Entity, S1 extends Service<E1>> Builder<E, S> join(
                final S1 joinService, final String localField) {
            aggregator.asMap.computeIfAbsent(DigestUtils.sha256Hex(joinService.getEntityClass().getName()), as -> {
                aggregator.pipelines.add(helper(joinService).lookup(localField, as));
                return joinService;
            });
            return this;
        }

        /**
         * Creates a $lookup pipeline stage, joining the current collection with the one specified in from
         * using the given pipeline.
         *
         * @param joinService   join的服务类
         * @param let           the variables to use in the pipeline field stages.
         * @param pipeline      the pipeline to run on the joined collection.
         * @param <E1>          join的实体类
         * @param <S1>          join的服务类
         * @param <TExpression> the Variable value expression type
         * @return Builder self
         */
        public <E1 extends Entity, S1 extends Service<E1>, TExpression> Builder<E, S> join(
                final S1 joinService, final List<Variable<TExpression>> let, final List<? extends Bson> pipeline) {
            aggregator.asMap.computeIfAbsent(DigestUtils.sha256Hex(joinService.getEntityClass().getName()), as -> {
                aggregator.pipelines.add(helper(joinService).lookup(let, pipeline, as));
                return joinService;
            });
            return this;
        }

        /**
         * 构造好的Aggregate实例.
         *
         * @return Aggregate实例
         */
        public Aggregator<E, S> build() {
            return aggregator;
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends Entity, S extends Service<E>> ServiceHelper<E> helper(final S service) {
        return (ServiceHelper<E>) service;
    }
}
