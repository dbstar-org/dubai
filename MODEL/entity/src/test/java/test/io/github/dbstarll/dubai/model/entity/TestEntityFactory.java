package test.io.github.dbstarll.dubai.model.entity;

import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.entity.EntityFactory.PojoFields;
import io.github.dbstarll.dubai.model.entity.EntityModifier;
import io.github.dbstarll.dubai.model.entity.test.*;
import io.github.dbstarll.dubai.model.entity.test.other.ClassPackageInterfaceEntity;
import junit.framework.TestCase;
import org.bson.types.ObjectId;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertNotEquals;

public class TestEntityFactory extends TestCase {
    /**
     * 测试newInstance一个类.
     */
    public void testNewInstanceWithClass() {
        final ClassEntity entity = EntityFactory.newInstance(ClassEntity.class);
        assertEquals(ClassEntity.class, EntityFactory.getEntityClass(entity));
        assertEquals(ClassEntity.class, EntityFactory.getEntityClass(entity.getClass()));
        assertEquals(ClassEntity.class, entity.getClass());
    }

    /**
     * 测试newInstance一个没有@Table的类.
     */
    public void testNewInstanceWithClassNoTable() {
        try {
            EntityFactory.newInstance(ClassNoTableEntity.class);
            fail("throw UnsupportedOperationException");
        } catch (UnsupportedOperationException ex) {
            assertEquals("Invalid EntityClass: " + ClassNoTableEntity.class, ex.getMessage());
        }
    }

    /**
     * 测试在newInstance一个抽象类时抛出UnsupportedOperationException异常.
     */
    public void testNewInstanceWithAbstractClass() {
        try {
            EntityFactory.newInstance(AbstractEntity.class);
            fail("throw UnsupportedOperationException");
        } catch (UnsupportedOperationException ex) {
            assertEquals("Invalid EntityClass: " + AbstractEntity.class, ex.getMessage());
        }
    }

    /**
     * 测试在newInstance一个无公开构造函数的类时抛出UnsupportedOperationException异常.
     */
    public void testNewInstanceWithNoPublicClass() {
        try {
            EntityFactory.newInstance(NoPublicClassEntity.class);
            fail("throw UnsupportedOperationException");
        } catch (UnsupportedOperationException ex) {
            assertEquals("Invalid EntityClass: " + NoPublicClassEntity.class, ex.getMessage());
        }
    }

    /**
     * 测试在newInstance一个会抛出异常的构造函数的类时抛出UnsupportedOperationException异常.
     */
    public void testNewInstanceWithThrowClass() {
        try {
            EntityFactory.newInstance(ThrowClassEntity.class);
            fail("throw UnsupportedOperationException");
        } catch (UnsupportedOperationException ex) {
            assertNotNull(ex.getCause());
            assertEquals("Instantiation fails: " + ThrowClassEntity.class, ex.getMessage());
            assertEquals(UnsupportedOperationException.class, ex.getCause().getClass());
            assertEquals("ThrowClassEntity", ex.getCause().getMessage());
        }
    }

    /**
     * 测试newInstance一个接口.
     */
    public void testNewInstanceWithInterface() {
        final InterfaceEntity entity = EntityFactory.newInstance(InterfaceEntity.class);
        assertEquals(InterfaceEntity.class, EntityFactory.getEntityClass(entity));
        assertEquals(InterfaceEntity.class, EntityFactory.getEntityClass(entity.getClass()));
        assertNotEquals(InterfaceEntity.class, entity.getClass());
        assertTrue(entity.toString().startsWith(InterfaceEntity.class.getName() + "{"));
    }

    /**
     * 测试在newInstance无Table注解的接口时抛出UnsupportedOperationException异常.
     */
    public void testNewInstanceWithInterfaceNoTable() {
        try {
            EntityFactory.newInstance(NoTableEntity.class);
            fail("throw UnsupportedOperationException");
        } catch (UnsupportedOperationException ex) {
            assertEquals("Invalid EntityClass: " + NoTableEntity.class, ex.getMessage());
        }
    }

    /**
     * 测试在newInstance时设置初始参数.
     */
    public void testNewInstanceWithFields() {
        final ObjectId id = new ObjectId();
        final Map<String, Object> fields = Collections.singletonMap(Entity.FIELD_NAME_ID, id);
        final InterfaceEntity entity = EntityFactory.newInstance(InterfaceEntity.class, fields);
        assertTrue(id == entity.getId());
        assertNull(entity.getDateCreated());
    }

    /**
     * 测试非EntityFactory的代理类调用getEntityClass.
     */
    public void testGetEntityClassNoEntityFactory() {
        final InterfaceEntity entity = (InterfaceEntity) Proxy.newProxyInstance(InterfaceEntity.class.getClassLoader(),
                new Class[]{EntityModifier.class, InterfaceEntity.class}, (proxy, method, args) -> null);
        assertNotEquals(InterfaceEntity.class, EntityFactory.getEntityClass(entity));
        assertEquals(InterfaceEntity.class, EntityFactory.getEntityClass(entity.getClass()));
        assertEquals(entity.getClass(), EntityFactory.getEntityClass(entity));
    }

    /**
     * 测试hashCode.
     */
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
        assertEquals(entity2.hashCode(), entity3.hashCode());
        assertNotEquals(entity1.hashCode(), entity2.hashCode());
        assertNotEquals(entity1.hashCode(), entity4.hashCode());
        assertNotEquals(entity2.hashCode(), entity4.hashCode());
    }

    /**
     * 测试equals.
     */
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
        assertEquals(entity2, entity3);
        assertNotEquals(entity1, entity2);
        assertNotEquals(entity1, entity4);
        assertNotEquals(entity2, entity4);

        assertNotEquals(entity1, null);
        assertNotEquals(entity1, new Object());
        assertEquals(entity1, entity1);
        assertNotEquals(entity1, Proxy.newProxyInstance(InterfaceEntity.class.getClassLoader(),
                new Class[]{InterfaceEntity.class}, (proxy, method, args) -> null));
        assertNotEquals(entity1, EntityFactory.newInstance(ClassPackageInterfaceEntity.class));
    }

    /**
     * 测试简单类型属性的缺省值.
     */
    public void testDefaultPrimitiveFields() {
        final InterfaceEntity entity = EntityFactory.newInstance(InterfaceEntity.class,
                Collections.singletonMap("booleanFromNoTableEntity", true));
        final Map<String, Object> fields = ((PojoFields) entity).fields();
        assertEquals(0, fields.get("intFromInterfaceEntity"));
        assertEquals(true, fields.get("booleanFromNoTableEntity"));
        assertFalse(fields.containsKey("stringFromInterfaceEntity"));
    }

    /**
     * 测试设置与获取属性.
     */
    public void testGetAndSet() {
        final InterfaceEntity entity = EntityFactory.newInstance(InterfaceEntity.class);
        assertNull(entity.getId());
        assertEquals(0, entity.getIntFromInterfaceEntity());
        assertEquals(false, entity.isBooleanFromNoTableEntity());
        assertEquals(null, entity.getStringFromInterfaceEntity());

        entity.setIntFromInterfaceEntity(100);
        assertEquals(100, entity.getIntFromInterfaceEntity());

        entity.setBooleanFromNoTableEntity(true);
        assertEquals(true, entity.isBooleanFromNoTableEntity());

        final String stringValue = new ObjectId().toHexString();
        entity.setStringFromInterfaceEntity(stringValue);
        assertTrue(stringValue == entity.getStringFromInterfaceEntity());
        entity.setStringFromInterfaceEntity(null);
        assertNull(entity.getStringFromInterfaceEntity());
    }

    /**
     * 测试一些不正常的get方法.
     */
    public void testGetInvalid() {
        final InterfaceEntity entity = EntityFactory.newInstance(InterfaceEntity.class);
        try {
            entity.getNoReturn();
            fail("throw UnsupportedOperationException");
        } catch (Throwable ex) {
            System.out.println(ex.getMessage());
            assertEquals(UnsupportedOperationException.class, ex.getClass());
        }
        try {
            entity.getIntWithParam(100);
            fail("throw UnsupportedOperationException");
        } catch (Throwable ex) {
            System.out.println(ex.getMessage());
            assertEquals(UnsupportedOperationException.class, ex.getClass());
        }
        try {
            entity.obtainInt();
            fail("throw UnsupportedOperationException");
        } catch (Throwable ex) {
            System.out.println(ex.getMessage());
            assertEquals(UnsupportedOperationException.class, ex.getClass());
        }
    }

    /**
     * 测试一些不正常的set方法.
     */
    public void testSetInvalid() {
        final InterfaceEntity entity = EntityFactory.newInstance(InterfaceEntity.class);
        try {
            entity.setIntWithReturn(100);
            fail("throw UnsupportedOperationException");
        } catch (Throwable ex) {
            System.out.println(ex.getMessage());
            assertEquals(UnsupportedOperationException.class, ex.getClass());
        }
        try {
            entity.setNoParam();
            fail("throw UnsupportedOperationException");
        } catch (Throwable ex) {
            System.out.println(ex.getMessage());
            assertEquals(UnsupportedOperationException.class, ex.getClass());
        }
        try {
            entity.giveInt(100);
            fail("throw UnsupportedOperationException");
        } catch (Throwable ex) {
            System.out.println(ex.getMessage());
            assertEquals(UnsupportedOperationException.class, ex.getClass());
        }
    }

    /**
     * 测试clone方法.
     */
    public void testClone() {
        final InterfaceEntity entity1 = EntityFactory.newInstance(InterfaceEntity.class);
        final String stringValue = new ObjectId().toHexString();
        entity1.setIntFromInterfaceEntity(100);
        entity1.setBooleanFromNoTableEntity(true);
        entity1.setStringFromInterfaceEntity(stringValue);

        final InterfaceEntity entity2 = EntityFactory.clone(entity1);

        assertFalse(entity1 == entity2);
        assertEquals(entity1.hashCode(), entity2.hashCode());
        assertEquals(entity1, entity2);

        assertTrue(stringValue == entity1.getStringFromInterfaceEntity());
        assertTrue(stringValue == entity2.getStringFromInterfaceEntity());
    }

    public void testCloneNone() {
        assertNull(EntityFactory.clone(null));
    }

    public void testCloneNoEntityModifier() {
        final NoModifierEntity entity = new NoModifierEntity();
        assertNotSame(entity, EntityFactory.clone(entity));
    }

    @Test
    public void testCloneException() {
        try {
            EntityFactory.clone(new NoCloneEntity());
            fail("throw UnsupportedOperationException");
        } catch (Throwable ex) {
            assertEquals(UnsupportedOperationException.class, ex.getClass());
            assertNotNull(ex.getCause());
            assertEquals(CloneNotSupportedException.class, ex.getCause().getClass());
            assertEquals("java.lang.CloneNotSupportedException", ex.getMessage());
        }
    }

    @Test
    public void testCloneNoEntityModifierException() {
        try {
            EntityFactory.clone(new NoModifierCloneEntity());
            fail("throw UnsupportedOperationException");
        } catch (Throwable ex) {
            assertEquals(UnsupportedOperationException.class, ex.getClass());
            assertNotNull(ex.getCause());
            assertEquals(CloneNotSupportedException.class, ex.getCause().getClass());
            assertEquals("Exception cloning Cloneable type " + NoModifierCloneEntity.class.getName(), ex.getMessage());
        }
    }
}
