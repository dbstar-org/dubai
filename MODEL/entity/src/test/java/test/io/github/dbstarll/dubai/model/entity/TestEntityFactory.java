package test.io.github.dbstarll.dubai.model.entity;

import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.entity.EntityFactory.PojoFields;
import io.github.dbstarll.dubai.model.entity.EntityModifier;
import io.github.dbstarll.dubai.model.entity.test.AbstractEntity;
import io.github.dbstarll.dubai.model.entity.test.ClassEntity;
import io.github.dbstarll.dubai.model.entity.test.ClassNoTableEntity;
import io.github.dbstarll.dubai.model.entity.test.FieldNoSerializable;
import io.github.dbstarll.dubai.model.entity.test.InterfaceEntity;
import io.github.dbstarll.dubai.model.entity.test.NoCloneEntity;
import io.github.dbstarll.dubai.model.entity.test.NoModifierCloneEntity;
import io.github.dbstarll.dubai.model.entity.test.NoModifierEntity;
import io.github.dbstarll.dubai.model.entity.test.NoPublicClassEntity;
import io.github.dbstarll.dubai.model.entity.test.NoTableEntity;
import io.github.dbstarll.dubai.model.entity.test.ThrowClassEntity;
import io.github.dbstarll.dubai.model.entity.test.other.ClassPackageInterfaceEntity;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertNotEquals;

public class TestEntityFactory {
    /**
     * 测试newInstance一个类.
     */
    @Test
    public void testNewInstanceWithClass() {
        final ClassEntity entity = EntityFactory.newInstance(ClassEntity.class);
        Assert.assertEquals(ClassEntity.class, EntityFactory.getEntityClass(entity));
        Assert.assertEquals(ClassEntity.class, EntityFactory.getEntityClass(entity.getClass()));
        Assert.assertEquals(ClassEntity.class, entity.getClass());
    }

    /**
     * 测试newInstance一个没有@Table的类.
     */
    @Test
    public void testNewInstanceWithClassNoTable() {
        try {
            EntityFactory.newInstance(ClassNoTableEntity.class);
            Assert.fail("throw UnsupportedOperationException");
        } catch (UnsupportedOperationException ex) {
            Assert.assertEquals("Invalid EntityClass: " + ClassNoTableEntity.class, ex.getMessage());
        }
    }

    /**
     * 测试在newInstance一个抽象类时抛出UnsupportedOperationException异常.
     */
    @Test
    public void testNewInstanceWithAbstractClass() {
        try {
            EntityFactory.newInstance(AbstractEntity.class);
            Assert.fail("throw UnsupportedOperationException");
        } catch (UnsupportedOperationException ex) {
            Assert.assertEquals("Invalid EntityClass: " + AbstractEntity.class, ex.getMessage());
        }
    }

    /**
     * 测试在newInstance一个无公开构造函数的类时抛出UnsupportedOperationException异常.
     */
    @Test
    public void testNewInstanceWithNoPublicClass() {
        try {
            EntityFactory.newInstance(NoPublicClassEntity.class);
            Assert.fail("throw UnsupportedOperationException");
        } catch (UnsupportedOperationException ex) {
            Assert.assertEquals("Invalid EntityClass: " + NoPublicClassEntity.class, ex.getMessage());
        }
    }

    /**
     * 测试在newInstance一个会抛出异常的构造函数的类时抛出UnsupportedOperationException异常.
     */
    @Test
    public void testNewInstanceWithThrowClass() {
        try {
            EntityFactory.newInstance(ThrowClassEntity.class);
            Assert.fail("throw UnsupportedOperationException");
        } catch (UnsupportedOperationException ex) {
            Assert.assertNotNull(ex.getCause());
            Assert.assertEquals("Instantiation fails: " + ThrowClassEntity.class, ex.getMessage());
            Assert.assertEquals(UnsupportedOperationException.class, ex.getCause().getClass());
            Assert.assertEquals("ThrowClassEntity", ex.getCause().getMessage());
        }
    }

    /**
     * 测试在newInstance一个包内可见的类时抛出UnsupportedOperationException异常.
     */
    @Test
    public void testNewInstanceWithPackageClass() {
        try {
            EntityFactory.newInstance(PackageClassEntity.class);
            Assert.fail("throw UnsupportedOperationException");
        } catch (UnsupportedOperationException ex) {
            Assert.assertNotNull(ex.getCause());
            Assert.assertEquals("Instantiation fails: " + PackageClassEntity.class, ex.getMessage());
            Assert.assertEquals(IllegalAccessException.class, ex.getCause().getClass());
        }
    }

    /**
     * 测试newInstance一个接口.
     */
    @Test
    public void testNewInstanceWithInterface() {
        final InterfaceEntity entity = EntityFactory.newInstance(InterfaceEntity.class);
        Assert.assertEquals(InterfaceEntity.class, EntityFactory.getEntityClass(entity));
        Assert.assertEquals(InterfaceEntity.class, EntityFactory.getEntityClass(entity.getClass()));
        assertNotEquals(InterfaceEntity.class, entity.getClass());
        Assert.assertTrue(entity.toString().startsWith(InterfaceEntity.class.getName() + "{"));
    }

    /**
     * 测试在newInstance无Table注解的接口时抛出UnsupportedOperationException异常.
     */
    @Test
    public void testNewInstanceWithInterfaceNoTable() {
        try {
            EntityFactory.newInstance(NoTableEntity.class);
            Assert.fail("throw UnsupportedOperationException");
        } catch (UnsupportedOperationException ex) {
            Assert.assertEquals("Invalid EntityClass: " + NoTableEntity.class, ex.getMessage());
        }
    }

    /**
     * 测试在newInstance时设置初始参数.
     */
    @Test
    public void testNewInstanceWithFields() {
        final ObjectId id = new ObjectId();
        final Map<String, Serializable> fields = Collections.singletonMap(Entity.FIELD_NAME_ID, id);
        final InterfaceEntity entity = EntityFactory.newInstance(InterfaceEntity.class, fields);
        Assert.assertSame(id, entity.getId());
        Assert.assertNull(entity.getDateCreated());
    }

    /**
     * 测试非EntityFactory的代理类调用getEntityClass.
     */
    @Test
    public void testGetEntityClassNoEntityFactory() {
        final InterfaceEntity entity = (InterfaceEntity) Proxy.newProxyInstance(InterfaceEntity.class.getClassLoader(),
                new Class[]{EntityModifier.class, InterfaceEntity.class}, (proxy, method, args) -> null);
        assertNotEquals(InterfaceEntity.class, EntityFactory.getEntityClass(entity));
        Assert.assertEquals(InterfaceEntity.class, EntityFactory.getEntityClass(entity.getClass()));
        Assert.assertEquals(entity.getClass(), EntityFactory.getEntityClass(entity));
    }

    /**
     * 测试hashCode.
     */
    @Test
    public void testHashCode() {
        final ObjectId id1 = new ObjectId();
        final ObjectId id2 = new ObjectId();
        final InterfaceEntity entity1 = EntityFactory.newInstance(InterfaceEntity.class);
        final InterfaceEntity entity2 = EntityFactory.newInstance(InterfaceEntity.class,
                Collections.singletonMap(Entity.FIELD_NAME_ID, id1));
        final InterfaceEntity entity3 = EntityFactory.newInstance(InterfaceEntity.class,
                Collections.singletonMap(Entity.FIELD_NAME_ID, id1));
        final InterfaceEntity entity4 = EntityFactory.newInstance(InterfaceEntity.class,
                Collections.singletonMap(Entity.FIELD_NAME_ID, id2));
        assertNotEquals(entity1.hashCode(), entity2.hashCode());
        Assert.assertEquals(entity2.hashCode(), entity3.hashCode());
        assertNotEquals(entity1.hashCode(), entity2.hashCode());
        assertNotEquals(entity1.hashCode(), entity4.hashCode());
        assertNotEquals(entity2.hashCode(), entity4.hashCode());
    }

    /**
     * 测试equals.
     */
    @Test
    public void testEquals() {
        final ObjectId id1 = new ObjectId();
        final ObjectId id2 = new ObjectId();
        final InterfaceEntity entity1 = EntityFactory.newInstance(InterfaceEntity.class);
        final InterfaceEntity entity2 = EntityFactory.newInstance(InterfaceEntity.class,
                Collections.singletonMap(Entity.FIELD_NAME_ID, id1));
        final InterfaceEntity entity3 = EntityFactory.newInstance(InterfaceEntity.class,
                Collections.singletonMap(Entity.FIELD_NAME_ID, id1));
        final InterfaceEntity entity4 = EntityFactory.newInstance(InterfaceEntity.class,
                Collections.singletonMap(Entity.FIELD_NAME_ID, id2));
        assertNotEquals(entity1, entity2);
        Assert.assertEquals(entity2, entity3);
        assertNotEquals(entity1, entity2);
        assertNotEquals(entity1, entity4);
        assertNotEquals(entity2, entity4);

        assertNotEquals(entity1, null);
        assertNotEquals(entity1, new Object());
        Assert.assertEquals(entity1, entity1);
        assertNotEquals(entity1, Proxy.newProxyInstance(InterfaceEntity.class.getClassLoader(),
                new Class[]{InterfaceEntity.class}, (proxy, method, args) -> null));
        assertNotEquals(entity1, EntityFactory.newInstance(ClassPackageInterfaceEntity.class));
    }

    /**
     * 测试简单类型属性的缺省值.
     */
    @Test
    public void testDefaultPrimitiveFields() {
        final InterfaceEntity entity = EntityFactory.newInstance(InterfaceEntity.class,
                Collections.singletonMap("booleanFromNoTableEntity", true));
        final Map<String, Object> fields = ((PojoFields) entity).fields();
        Assert.assertEquals(0, fields.get("intFromInterfaceEntity"));
        Assert.assertEquals(true, fields.get("booleanFromNoTableEntity"));
        Assert.assertFalse(fields.containsKey("stringFromInterfaceEntity"));
    }

    /**
     * 测试设置与获取属性.
     */
    @Test
    public void testGetAndSet() {
        final InterfaceEntity entity = EntityFactory.newInstance(InterfaceEntity.class);
        Assert.assertNull(entity.getId());
        Assert.assertEquals(0, entity.getIntFromInterfaceEntity());
        Assert.assertFalse(entity.isBooleanFromNoTableEntity());
        Assert.assertNull(entity.getStringFromInterfaceEntity());

        entity.setIntFromInterfaceEntity(100);
        Assert.assertEquals(100, entity.getIntFromInterfaceEntity());

        entity.setBooleanFromNoTableEntity(true);
        Assert.assertTrue(entity.isBooleanFromNoTableEntity());

        final String stringValue = new ObjectId().toHexString();
        entity.setStringFromInterfaceEntity(stringValue);
        Assert.assertSame(stringValue, entity.getStringFromInterfaceEntity());
        entity.setStringFromInterfaceEntity(null);
        Assert.assertNull(entity.getStringFromInterfaceEntity());
    }

    /**
     * 测试一些不正常的get方法.
     */
    @Test
    public void testGetInvalid() {
        final InterfaceEntity entity = EntityFactory.newInstance(InterfaceEntity.class);
        try {
            entity.getNoReturn();
            Assert.fail("throw UnsupportedOperationException");
        } catch (Throwable ex) {
            Assert.assertEquals(UnsupportedOperationException.class, ex.getClass());
        }
        try {
            entity.getIntWithParam(100);
            Assert.fail("throw UnsupportedOperationException");
        } catch (Throwable ex) {
            Assert.assertEquals(UnsupportedOperationException.class, ex.getClass());
        }
        try {
            entity.obtainInt();
            Assert.fail("throw UnsupportedOperationException");
        } catch (Throwable ex) {
            Assert.assertEquals(UnsupportedOperationException.class, ex.getClass());
        }
    }

    /**
     * 测试一些不正常的set方法.
     */
    @Test
    public void testSetInvalid() {
        final InterfaceEntity entity = EntityFactory.newInstance(InterfaceEntity.class);
        try {
            entity.setIntWithReturn(100);
            Assert.fail("throw UnsupportedOperationException");
        } catch (Throwable ex) {
            Assert.assertEquals(UnsupportedOperationException.class, ex.getClass());
        }
        try {
            entity.setNoParam();
            Assert.fail("throw UnsupportedOperationException");
        } catch (Throwable ex) {
            Assert.assertEquals(UnsupportedOperationException.class, ex.getClass());
        }
        try {
            entity.setTwoParam(1, "2");
            Assert.fail("throw UnsupportedOperationException");
        } catch (Throwable ex) {
            Assert.assertEquals(UnsupportedOperationException.class, ex.getClass());
        }
        try {
            entity.setField(new FieldNoSerializable());
            Assert.fail("throw UnsupportedOperationException");
        } catch (Throwable ex) {
            Assert.assertEquals(UnsupportedOperationException.class, ex.getClass());
        }
        try {
            entity.giveInt(100);
            Assert.fail("throw UnsupportedOperationException");
        } catch (Throwable ex) {
            Assert.assertEquals(UnsupportedOperationException.class, ex.getClass());
        }
    }

    /**
     * 测试clone方法.
     */
    @Test
    public void testClone() {
        final InterfaceEntity entity1 = EntityFactory.newInstance(InterfaceEntity.class);
        final String stringValue = new ObjectId().toHexString();
        entity1.setIntFromInterfaceEntity(100);
        entity1.setBooleanFromNoTableEntity(true);
        entity1.setStringFromInterfaceEntity(stringValue);

        final InterfaceEntity entity2 = EntityFactory.clone(entity1);

        Assert.assertNotSame(entity1, entity2);
        Assert.assertEquals(entity1.hashCode(), entity2.hashCode());
        Assert.assertEquals(entity1, entity2);

        Assert.assertSame(stringValue, entity1.getStringFromInterfaceEntity());
        Assert.assertSame(stringValue, entity2.getStringFromInterfaceEntity());
    }

    @Test
    public void testCloneNone() {
        Assert.assertNull(EntityFactory.clone(null));
    }

    @Test
    public void testCloneNoEntityModifier() {
        final NoModifierEntity entity = new NoModifierEntity();
        Assert.assertNotSame(entity, EntityFactory.clone(entity));
    }

    @Test
    public void testCloneException() {
        try {
            EntityFactory.clone(new NoCloneEntity());
            Assert.fail("throw UnsupportedOperationException");
        } catch (Throwable ex) {
            Assert.assertEquals(UnsupportedOperationException.class, ex.getClass());
            Assert.assertNotNull(ex.getCause());
            Assert.assertEquals(CloneNotSupportedException.class, ex.getCause().getClass());
            Assert.assertEquals("java.lang.CloneNotSupportedException", ex.getMessage());
        }
    }

    @Test
    public void testCloneNoEntityModifierException() {
        try {
            EntityFactory.clone(new NoModifierCloneEntity());
            Assert.fail("throw UnsupportedOperationException");
        } catch (Throwable ex) {
            Assert.assertEquals(UnsupportedOperationException.class, ex.getClass());
            Assert.assertNotNull(ex.getCause());
            Assert.assertEquals(CloneNotSupportedException.class, ex.getCause().getClass());
            Assert.assertEquals("Exception cloning Cloneable type " + NoModifierCloneEntity.class.getName(), ex.getMessage());
        }
    }
}
