package io.github.dbstarll.dubai.model.mongodb;

import io.github.dbstarll.dubai.model.collection.test.SimpleGenericEntity;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.EntityConvention;
import org.bson.codecs.pojo.InstanceCreator;
import org.bson.codecs.pojo.InstanceCreatorFactory;
import org.bson.codecs.pojo.PropertyModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

public class TestEntityInstanceCreatorFactory {
    private InstanceCreatorFactory<SimpleGenericEntity> instanceCreatorFactory;

    @BeforeEach
    public void initialize() {
        this.instanceCreatorFactory = new EntityInstanceCreatorFactory<>(SimpleGenericEntity.class);
    }

    @Test
    public void testCreate() {
        assertNotNull(instanceCreatorFactory.create());
        assertNotSame(instanceCreatorFactory.create(), instanceCreatorFactory.create());
    }

    /**
     * 测试getInstance.
     */
    @Test
    public void testGetInstance() {
        final InstanceCreator<SimpleGenericEntity> instanceCreator = instanceCreatorFactory.create();
        assertNotNull(instanceCreator.getInstance());
        assertSame(instanceCreator.getInstance(), instanceCreator.getInstance());
        assertNotSame(instanceCreator.getInstance(), instanceCreatorFactory.create().getInstance());
    }

    /**
     * 测试set方法.
     */
    @Test
    public void testSet() {
        final InstanceCreator<SimpleGenericEntity> instanceCreator = instanceCreatorFactory.create();
        final Integer value = 100;

        final ClassModel<SimpleGenericEntity> classModel = ClassModel.builder(SimpleGenericEntity.class)
                .conventions(Collections.singletonList(new EntityConvention()))
                .build();
        //noinspection unchecked
        final PropertyModel<Integer> propertyModel = (PropertyModel<Integer>) classModel.getPropertyModel("value");

        instanceCreator.set(value, propertyModel);
        final SimpleGenericEntity entity = instanceCreator.getInstance();
        assertEquals(value, entity.getValue());
    }
}
