package test.io.github.dbstarll.dubai.model.mongodb;

import io.github.dbstarll.dubai.model.collection.test.SimpleGenericEntity;
import io.github.dbstarll.dubai.model.mongodb.EntityInstanceCreatorFactory;
import mockit.Expectations;
import mockit.Mocked;
import org.bson.codecs.pojo.InstanceCreator;
import org.bson.codecs.pojo.InstanceCreatorFactory;
import org.bson.codecs.pojo.PropertyAccessor;
import org.bson.codecs.pojo.PropertyModel;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestEntityInstanceCreatorFactory {
    private InstanceCreatorFactory<SimpleGenericEntity> instanceCreatorFactory;

    @Mocked
    PropertyModel<Integer> propertyModel;

    @Before
    public void initialize() {
        this.instanceCreatorFactory = new EntityInstanceCreatorFactory<>(SimpleGenericEntity.class);
    }

    @Test
    public void testCreate() {
        assertNotNull(instanceCreatorFactory.create());
        assertTrue(instanceCreatorFactory.create() != instanceCreatorFactory.create());
    }

    /**
     * 测试getInstance.
     */
    @Test
    public void testGetInstance() {
        final InstanceCreator<SimpleGenericEntity> instanceCreator = instanceCreatorFactory.create();
        assertNotNull(instanceCreator.getInstance());
        assertTrue(instanceCreator.getInstance() == instanceCreator.getInstance());
        assertTrue(instanceCreator.getInstance() != instanceCreatorFactory.create().getInstance());
    }

    /**
     * 测试set方法.
     */
    @Test
    public void testSet() {
        final InstanceCreator<SimpleGenericEntity> instanceCreator = instanceCreatorFactory.create();
        final Integer value = new Integer(100);

        new Expectations() {
            {
                propertyModel.getPropertyAccessor();
                result = new PropertyAccessor<Integer>() {
                    @Override
                    public <S> Integer get(S instance) {
                        return null;
                    }

                    @Override
                    public <S> void set(S instance, Integer value) {
                        ((SimpleGenericEntity) instance).setValue(value);
                    }
                };
            }
        };

        instanceCreator.set(value, propertyModel);
        final SimpleGenericEntity entity = instanceCreator.getInstance();
        assertEquals(value, entity.getValue());
    }
}
