package io.github.dbstarll.dubai.model.service.validation;

import io.github.dbstarll.dubai.model.collection.test.SimpleEntity;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

class ValidationContextTest {
    @Test
    void getEntity() {
        try (ValidationContext context = ValidationContext.get()) {
            final ObjectId entityId = new ObjectId();
            final SimpleEntity entity = EntityFactory.newInstance(SimpleEntity.class);
            assertFalse(context.getEntity(null, null).isPresent());
            assertFalse(context.getEntity(entityId, null).isPresent());
            context.getEntity(entityId, key -> entity).ifPresent(e -> assertSame(entity, e));
            context.getEntity(entityId, null).ifPresent(e -> assertSame(entity, e));
        }
    }
}