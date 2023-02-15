package io.github.dbstarll.dubai.model.entity.utils;

import io.github.dbstarll.dubai.model.entity.test.ClassEntity;
import io.github.dbstarll.dubai.model.entity.test.InterfaceEntity;
import org.junit.jupiter.api.Test;

import java.beans.PropertyDescriptor;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestEntityUtils {
    @Test
    void testNewInstance() {
        assertThrows(IllegalAccessException.class, EntityUtils.class::newInstance);
    }

    @Test
    void testPropertyDescriptors() {
        final Set<String> keys = new TreeSet<>();
        for (PropertyDescriptor pd : EntityUtils.propertyDescriptors(InterfaceEntity.class)) {
            keys.add(pd.getName());
        }
        assertEquals("[booleanFromNoTableEntity, dateCreated, defunct, field, id, intFromInterfaceEntity,"
                + " intWithParam, lastModified, stringFromInterfaceEntity, twoParam, type]", keys.toString());
    }

    @Test
    void testPropertyDescriptor() {
        final PropertyDescriptor id = EntityUtils.propertyDescriptor(InterfaceEntity.class, "id");
        assertNotNull(id);
        assertNotNull(id.getReadMethod());
        assertNull(id.getWriteMethod());

        assertNotNull(EntityUtils.getReadMethod(id));
        assertNull(EntityUtils.getWriteMethod(id));

        assertNull(EntityUtils.propertyDescriptor(InterfaceEntity.class, "oid"));
    }

    @Test
    void testClassEntity() {
        final Set<String> keys = new TreeSet<>();
        for (PropertyDescriptor pd : EntityUtils.propertyDescriptors(ClassEntity.class)) {
            keys.add(pd.getName());
        }
        assertEquals("[dateCreated, id, lastModified]", keys.toString());
    }
}
