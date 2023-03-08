package io.github.dbstarll.dubai.model.service;

import io.github.dbstarll.dubai.model.MongodTestCase;
import io.github.dbstarll.dubai.model.collection.test.SimpleEntity;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.service.test.TestEntity;
import io.github.dbstarll.dubai.model.service.test.TestEntityService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidationContextTest extends ServiceTestCase {
    @BeforeAll
    public static void setup() {
        MongodTestCase.globalCollectionFactory();
    }

    @Test
    void getEntity() {
        try (ValidationContext ignored = ValidationContext.get().clear()) {
            final ObjectId entityId = new ObjectId();
            final SimpleEntity entity = EntityFactory.newInstance(SimpleEntity.class);

            assertFalse(ValidationContext.getEntity(null).isPresent());
            assertFalse(ValidationContext.getEntity(entityId).isPresent());

            final Optional<SimpleEntity> o1 = ValidationContext.getEntity(entityId, key -> entity);
            assertTrue(o1.isPresent());
            o1.ifPresent(e -> assertSame(entity, e));
            final Optional<SimpleEntity> o2 = ValidationContext.getEntity(entityId);
            assertTrue(o2.isPresent());
            o2.ifPresent(e -> assertSame(entity, e));
        }
    }

    @Test
    void getEntityWithService() {
        useService(TestEntityService.class, service -> {
            try (ValidationContext ignored = ValidationContext.get().clear()) {
                final TestEntity entity = EntityFactory.newInstance(TestEntity.class);
                assertSame(entity, service.save(entity, null));

                assertFalse(ValidationContext.getEntity(null).isPresent());
                assertFalse(ValidationContext.getEntity(entity.getId()).isPresent());

                final Optional<TestEntity> o1 = ValidationContext.getEntity(entity.getId(), service);
                assertTrue(o1.isPresent());
                o1.ifPresent(e -> assertEquals(entity, e));
                final Optional<TestEntity> o2 = ValidationContext.getEntity(entity.getId());
                assertTrue(o2.isPresent());
                o2.ifPresent(e -> assertEquals(entity, e));
            }
        });
    }
}