package io.github.dbstarll.dubai.model.service;

import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Variable;
import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.entity.join.CompanyBase;
import io.github.dbstarll.dubai.model.service.test.TestEntity;
import io.github.dbstarll.dubai.model.service.test.TestEntityService;
import io.github.dbstarll.dubai.model.service.test3.namable.TestNamableEntity;
import io.github.dbstarll.dubai.model.service.test3.namable.TestNamableService;
import io.github.dbstarll.utils.lang.wrapper.EntryWrapper;
import org.bson.Document;
import org.bson.codecs.DecoderContext;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AggregatorTest extends ServiceTestCase {
    private static final DecoderContext DEFAULT_CONTEXT = DecoderContext.builder().checkedDiscriminator(true).build();

    private final Class<TestEntity> entityClass = TestEntity.class;
    private final Class<TestEntityService> serviceClass = TestEntityService.class;

    @BeforeAll
    static void setup() {
        globalCollectionFactory();
    }

    @Test
    void joinOne() {
        useCollectionFactory(cf -> {
            final Collection<TestEntity> collection = cf.newInstance(entityClass);
            final TestEntityService service = ServiceFactory.newInstance(serviceClass, cf.newInstance(entityClass));
            final TestNamableService companyService = ServiceFactory.newInstance(TestNamableService.class,
                    cf.newInstance(TestNamableEntity.class));

            final Aggregator<TestEntity, TestEntityService> aggregator = Aggregator.builder(service, collection)
                    .match(null)
                    .join(companyService, CompanyBase.FIELD_NAME_COMPANY_ID)
                    .build();

            assertNull(aggregator.joinOne(DEFAULT_CONTEXT)
                    .map(e -> EntryWrapper.wrap(e.getKey(), e.getValue().get(companyService.getEntityClass())))
                    .first());

            final TestNamableEntity company = EntityFactory.newInstance(TestNamableEntity.class);
            company.setName("测试公司");
            assertNotNull(companyService.save(company, null));

            final TestEntity entity = service.save(EntityFactory.newInstance(entityClass), null);
            assertNotNull(entity);

            final Entry<TestEntity, Entity> match = aggregator.joinOne(DEFAULT_CONTEXT)
                    .map(e -> EntryWrapper.wrap(e.getKey(), e.getValue().get(companyService.getEntityClass())))
                    .first();
            assertNotNull(match);
            assertEquals(entity, match.getKey());
            assertNull(match.getValue());

            entity.setCompanyId(company.getId());
            assertNotNull(service.save(entity, null));

            final Entry<TestEntity, Entity> match2 = aggregator.joinOne(DEFAULT_CONTEXT)
                    .map(e -> EntryWrapper.wrap(e.getKey(), e.getValue().get(companyService.getEntityClass())))
                    .first();
            assertNotNull(match2);
            assertEquals(entity, match2.getKey());
            assertEquals(company, match2.getValue());
        });
    }

    @Test
    void join() {
        useCollectionFactory(cf -> {
            final Collection<TestEntity> collection = cf.newInstance(entityClass);
            final TestEntityService service = ServiceFactory.newInstance(serviceClass, cf.newInstance(entityClass));
            final TestNamableService companyService = ServiceFactory.newInstance(TestNamableService.class,
                    cf.newInstance(TestNamableEntity.class));

            final TestNamableEntity company = EntityFactory.newInstance(TestNamableEntity.class);
            company.setName("测试公司");
            assertNotNull(companyService.save(company, null));

            final TestEntity entity = service.save(EntityFactory.newInstance(entityClass), null);
            assertNotNull(entity);

            final Aggregator<TestEntity, TestEntityService> aggregator = Aggregator.builder(service, collection)
                    .match(Filters.eq(entity.getId()))
                    .join(companyService, CompanyBase.FIELD_NAME_COMPANY_ID)
                    .build();

            final Entry<TestEntity, List<Entity>> match = aggregator.join(DEFAULT_CONTEXT)
                    .map(e -> EntryWrapper.wrap(e.getKey(), e.getValue().get(companyService.getEntityClass())))
                    .first();
            assertNotNull(match);
            assertEquals(entity, match.getKey());
            assertNotNull(match.getValue());
            assertEquals(0, match.getValue().size());

            entity.setCompanyId(company.getId());
            assertNotNull(service.save(entity, null));

            final Entry<TestEntity, List<Entity>> match2 = aggregator.join(DEFAULT_CONTEXT)
                    .map(e -> EntryWrapper.wrap(e.getKey(), e.getValue().get(companyService.getEntityClass())))
                    .first();
            assertNotNull(match2);
            assertEquals(entity, match2.getKey());
            assertNotNull(match2.getValue());
            assertEquals(1, match2.getValue().size());
            assertEquals(company, match2.getValue().get(0));
        });
    }

    @Test
    void testAggregateMatchFilter() {
        useService(TestNamableService.class, s -> {
            assertNull(s.filter(null));
            final Bson f = Filters.eq(new ObjectId());
            assertSame(f, s.filter(f));
        });
    }

    @Test
    void sample() {
        useCollectionFactory(cf -> {
            final Collection<TestEntity> collection = cf.newInstance(entityClass);
            final TestEntityService service = ServiceFactory.newInstance(serviceClass, cf.newInstance(entityClass));

            final TestEntity entity = service.save(EntityFactory.newInstance(entityClass), null);
            assertNotNull(entity);

            final Aggregator<TestEntity, TestEntityService> aggregator = Aggregator.builder(service, collection)
                    .sample(1)
                    .build();

            final List<TestEntity> match = aggregator.join(DEFAULT_CONTEXT).map(Entry::getKey).into(new ArrayList<>());
            assertEquals(1, match.size());
            assertEquals(entity, match.get(0));

            final TestEntity entity2 = service.save(EntityFactory.newInstance(entityClass), null);
            assertNotNull(entity2);

            final AtomicInteger match1 = new AtomicInteger();
            final AtomicInteger match2 = new AtomicInteger();
            for (int i = 0; i < 10; i++) {
                final List<TestEntity> matchAgain = aggregator.join(DEFAULT_CONTEXT).map(Entry::getKey).into(new ArrayList<>());
                assertEquals(1, matchAgain.size());
                if (entity.equals(matchAgain.get(0))) {
                    match1.incrementAndGet();
                } else if (entity2.equals(matchAgain.get(0))) {
                    match2.incrementAndGet();
                }
            }
            assertTrue(match1.get() > 0);
            assertTrue(match2.get() > 0);
            assertEquals(10, match1.get() + match2.get());
        });
    }

    @Test
    void aggregate() {
        useCollectionFactory(cf -> {
            final Collection<TestEntity> collection = cf.newInstance(entityClass);
            final TestEntityService service = ServiceFactory.newInstance(serviceClass, cf.newInstance(entityClass));

            assertNull(Aggregator.builder(service, collection)
                    .sort(null).skip(0).limit(0).project(null).sample(0)
                    .build().aggregate(TestEntity.class).first());

            final TestEntity entity1 = service.save(EntityFactory.newInstance(entityClass), null);
            final TestEntity entity2 = service.save(EntityFactory.newInstance(entityClass), null);

            final List<TestEntity> descendingList = Aggregator.builder(service, collection)
                    .sort(Sorts.descending("dateCreated"))
                    .build().aggregate(TestEntity.class).into(new ArrayList<>());
            assertEquals(2, descendingList.size());
            assertEquals(entity2, descendingList.get(0));
            assertEquals(entity1, descendingList.get(1));

            final List<TestEntity> ascendingList = Aggregator.builder(service, collection)
                    .sort(Sorts.ascending("dateCreated"))
                    .build().aggregate(TestEntity.class).into(new ArrayList<>());
            assertEquals(2, ascendingList.size());
            assertEquals(entity1, ascendingList.get(0));
            assertEquals(entity2, ascendingList.get(1));

            final List<TestEntity> limitList = Aggregator.builder(service, collection)
                    .sort(Sorts.descending("dateCreated")).limit(1)
                    .build().aggregate(TestEntity.class).into(new ArrayList<>());
            assertEquals(1, limitList.size());
            assertEquals(entity2, limitList.get(0));

            final List<TestEntity> skipList = Aggregator.builder(service, collection)
                    .sort(Sorts.descending("dateCreated")).skip(1)
                    .build().aggregate(TestEntity.class).into(new ArrayList<>());
            assertEquals(1, skipList.size());
            assertEquals(entity1, skipList.get(0));

            final List<TestEntity> projectList = Aggregator.builder(service, collection)
                    .sort(Sorts.descending("dateCreated"))
                    .project(Projections.exclude("lastModified"))
                    .build().aggregate(TestEntity.class).into(new ArrayList<>());
            assertEquals(2, projectList.size());
            assertEquals(entity2.getId(), projectList.get(0).getId());
            assertEquals(entity1.getId(), projectList.get(1).getId());
            assertNull(projectList.get(0).getLastModified());
            assertNull(projectList.get(1).getLastModified());
        });
    }

    @Test
    void group() {
        useCollectionFactory(cf -> {
            final Collection<TestEntity> collection = cf.newInstance(entityClass);
            final TestEntityService service = ServiceFactory.newInstance(serviceClass, cf.newInstance(entityClass));

            final ObjectId companyId1 = new ObjectId();
            final ObjectId companyId2 = new ObjectId();

            final TestEntity entity1 = EntityFactory.newInstance(entityClass);
            entity1.setCompanyId(companyId1);
            assertSame(entity1, service.save(entity1, null));

            final TestEntity entity2 = EntityFactory.newInstance(entityClass);
            entity2.setCompanyId(companyId1);
            assertSame(entity2, service.save(entity2, null));

            final TestEntity entity3 = EntityFactory.newInstance(entityClass);
            entity3.setCompanyId(companyId2);
            assertSame(entity3, service.save(entity3, null));

            final List<Document> list = Aggregator.builder(service, collection)
                    .group("$" + CompanyBase.FIELD_NAME_COMPANY_ID, Accumulators.sum("count", 1))
                    .sort(Sorts.ascending("count"))
                    .build().aggregate(Document.class).into(new ArrayList<>());
            assertEquals(2, list.size());
            assertEquals(companyId2, list.get(0).getObjectId(Entity.FIELD_NAME_ID));
            assertEquals(companyId1, list.get(1).getObjectId(Entity.FIELD_NAME_ID));
            assertEquals(1, list.get(0).getInteger("count"));
            assertEquals(2, list.get(1).getInteger("count"));
        });
    }

    @Test
    void lookup() {
        useCollectionFactory(cf -> {
            final Collection<TestEntity> collection = cf.newInstance(entityClass);
            final TestEntityService service = ServiceFactory.newInstance(serviceClass, cf.newInstance(entityClass));
            final TestNamableService companyService = ServiceFactory.newInstance(TestNamableService.class,
                    cf.newInstance(TestNamableEntity.class));

            final Variable<String> letCompanyId = new Variable<>("companyId", "$" + CompanyBase.FIELD_NAME_COMPANY_ID);
            final Bson filterCompanyId = Filters.expr(Filters.eq("$eq", Arrays.asList("$" + Entity.FIELD_NAME_ID, "$$companyId")));

            final Aggregator<TestEntity, TestEntityService> aggregator = Aggregator.builder(service, collection)
                    .join(companyService, Collections.singletonList(letCompanyId), Collections.singletonList(Aggregates.match(filterCompanyId)))
                    .build();

            assertNull(aggregator.joinOne(DEFAULT_CONTEXT)
                    .map(e -> EntryWrapper.wrap(e.getKey(), e.getValue().get(companyService.getEntityClass())))
                    .first());

            final TestNamableEntity company = EntityFactory.newInstance(TestNamableEntity.class);
            company.setName("测试公司");
            assertNotNull(companyService.save(company, null));

            final TestEntity entity = service.save(EntityFactory.newInstance(entityClass), null);
            assertNotNull(entity);

            final Entry<TestEntity, Entity> match = aggregator.joinOne(DEFAULT_CONTEXT)
                    .map(e -> EntryWrapper.wrap(e.getKey(), e.getValue().get(companyService.getEntityClass())))
                    .first();
            assertNotNull(match);
            assertEquals(entity, match.getKey());
            assertNull(match.getValue());

            entity.setCompanyId(company.getId());
            assertNotNull(service.save(entity, null));

            final Entry<TestEntity, Entity> match2 = aggregator.joinOne(DEFAULT_CONTEXT)
                    .map(e -> EntryWrapper.wrap(e.getKey(), e.getValue().get(companyService.getEntityClass())))
                    .first();
            assertNotNull(match2);
            assertEquals(entity, match2.getKey());
            assertEquals(company, match2.getValue());
        });
    }
}