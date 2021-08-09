package test.io.github.dbstarll.dubai.model.entity.utils;

import io.github.dbstarll.dubai.model.entity.test.ClassEntity;
import io.github.dbstarll.dubai.model.entity.test.InterfaceEntity;
import io.github.dbstarll.dubai.model.entity.utils.EntityUtils;
import junit.framework.TestCase;
import org.junit.Test;

import java.beans.PropertyDescriptor;
import java.util.Set;
import java.util.TreeSet;

public class TestEntityUtils extends TestCase {
    @Test
    public void testNew() {
        new EntityUtils();
    }

    @Test
    public void testPropertyDescriptors() {
        final Set<String> keys = new TreeSet<>();
        for (PropertyDescriptor pd : EntityUtils.propertyDescriptors(InterfaceEntity.class)) {
            keys.add(pd.getName());
        }
        assertEquals("[booleanFromNoTableEntity, dateCreated, defunct, id, intFromInterfaceEntity,"
                + " intWithParam, lastModified, stringFromInterfaceEntity, type]", keys.toString());
    }

    @Test
    public void testPropertyDescriptor() {
        final PropertyDescriptor id = EntityUtils.propertyDescriptor(InterfaceEntity.class, "id");
        assertNotNull(id);
        assertNotNull(id.getReadMethod());
        assertNull(id.getWriteMethod());

        assertNotNull(EntityUtils.getReadMethod(id));
        assertNull(EntityUtils.getWriteMethod(id));

        assertNull(EntityUtils.propertyDescriptor(InterfaceEntity.class, "oid"));
    }

    @Test
    public void testClassEntity() {
        final Set<String> keys = new TreeSet<>();
        for (PropertyDescriptor pd : EntityUtils.propertyDescriptors(ClassEntity.class)) {
            keys.add(pd.getName());
        }
        assertEquals("[dateCreated, id, lastModified]", keys.toString());
    }
}
