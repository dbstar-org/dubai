package io.github.dbstarll.dubai.model.service;

import com.mongodb.client.model.Filters;
import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.entity.join.CompanyBase;
import io.github.dbstarll.dubai.model.service.test.TestEntity;
import io.github.dbstarll.dubai.model.service.test.TestEntityService;
import io.github.dbstarll.dubai.model.service.test3.namable.TestNamableEntity;
import io.github.dbstarll.dubai.model.service.test3.namable.TestNamableService;
import io.github.dbstarll.utils.lang.wrapper.EntryWrapper;
import org.bson.codecs.DecoderContext;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Map.Entry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class AggregatorTest extends ServiceTestCase {
    private static final DecoderContext DEFAULT_CONTEXT = DecoderContext.builder().checkedDiscriminator(true).build();

    private final Class<TestEntity> entityClass = TestEntity.class;
    private final Class<TestEntityService> serviceClass = TestEntityService.class;

    @BeforeClass
    public static void setup() {
        globalCollectionFactory();
    }

    @Test
    public void aggregateOne() {
        useCollectionFactory(cf -> {
            final Collection<TestEntity> collection = cf.newInstance(entityClass);
            final TestEntityService service = ServiceFactory.newInstance(serviceClass, cf.newInstance(entityClass));
            final TestNamableService companyService = ServiceFactory.newInstance(TestNamableService.class,
                    cf.newInstance(TestNamableEntity.class));

            final Aggregator<TestEntity, TestEntityService> aggregator = Aggregator.builder(service, collection)
                    .match(null)
                    .join(companyService, CompanyBase.FIELD_NAME_COMPANY_ID)
                    .build();

            assertNull(aggregator.aggregateOne(DEFAULT_CONTEXT)
                    .map(e -> EntryWrapper.wrap(e.getKey(), e.getValue().get(companyService.getEntityClass())))
                    .first());

            final TestNamableEntity company = EntityFactory.newInstance(TestNamableEntity.class);
            company.setName("测试公司");
            assertNotNull(companyService.save(company, null));

            final TestEntity entity = service.save(EntityFactory.newInstance(entityClass), null);
            assertNotNull(entity);

            final Entry<TestEntity, Entity> match = aggregator.aggregateOne(DEFAULT_CONTEXT)
                    .map(e -> EntryWrapper.wrap(e.getKey(), e.getValue().get(companyService.getEntityClass())))
                    .first();
            assertNotNull(match);
            assertEquals(entity, match.getKey());
            assertNull(match.getValue());

            entity.setCompanyId(company.getId());
            assertNotNull(service.save(entity, null));

            final Entry<TestEntity, Entity> match2 = aggregator.aggregateOne(DEFAULT_CONTEXT)
                    .map(e -> EntryWrapper.wrap(e.getKey(), e.getValue().get(companyService.getEntityClass())))
                    .first();
            assertNotNull(match2);
            assertEquals(entity, match2.getKey());
            assertEquals(company, match2.getValue());
        });
    }

    @Test
    public void aggregate() {
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

            final Entry<TestEntity, List<Entity>> match = aggregator.aggregate(DEFAULT_CONTEXT)
                    .map(e -> EntryWrapper.wrap(e.getKey(), e.getValue().get(companyService.getEntityClass())))
                    .first();
            assertNotNull(match);
            assertEquals(entity, match.getKey());
            assertNotNull(match.getValue());
            assertEquals(0, match.getValue().size());

            entity.setCompanyId(company.getId());
            assertNotNull(service.save(entity, null));

            final Entry<TestEntity, List<Entity>> match2 = aggregator.aggregate(DEFAULT_CONTEXT)
                    .map(e -> EntryWrapper.wrap(e.getKey(), e.getValue().get(companyService.getEntityClass())))
                    .first();
            assertNotNull(match2);
            assertEquals(entity, match2.getKey());
            assertNotNull(match2.getValue());
            assertEquals(1, match2.getValue().size());
            assertEquals(company, match2.getValue().get(0));
        });
    }
}