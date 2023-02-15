package io.github.dbstarll.dubai.model.service.validation;

import io.github.dbstarll.dubai.model.entity.EntityFactory;
import io.github.dbstarll.dubai.model.entity.test.InterfaceEntity;
import io.github.dbstarll.dubai.model.entity.test.o2.PublicPackageInterfaceEntity;
import io.github.dbstarll.dubai.model.service.validate.Validate;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class TestMultiValidation {
    /**
     * 测试构造函数中传入null的validations.
     */
    public void testCreateWithNullValidations() {
        try {
            new MultiValidation<>(InterfaceEntity.class, (Validation<InterfaceEntity>[]) null);
            fail("throw NullPointerException");
        } catch (Exception ex) {
            assertEquals(NullPointerException.class, ex.getClass());
            assertEquals("validations is null", ex.getMessage());
        }
    }

    /**
     * 测试构造函数中传入null的validation.
     */
    public void testCreateWithNullValidation() {
        try {
            new MultiValidation<>(InterfaceEntity.class, (Validation<InterfaceEntity>) null);
            fail("throw NullPointerException");
        } catch (Exception ex) {
            assertEquals(IllegalArgumentException.class, ex.getClass());
            assertEquals("validations contains null element at index: 0", ex.getMessage());
        }
    }

    /**
     * 测试构造函数中传入null的validation.
     */
    public void testCreateWithNullEntityClass() {
        try {
            new MultiValidation<>(null);
            fail("throw NullPointerException");
        } catch (Exception ex) {
            assertEquals(NullPointerException.class, ex.getClass());
            assertEquals("entityClass is null", ex.getMessage());
        }
    }

    /**
     * 测试hashCode.
     */
    public void testHashCode() {
        final MultiValidation<InterfaceEntity> mv1 = new MultiValidation<>(InterfaceEntity.class);
        final MultiValidation<InterfaceEntity> mv2 = new MultiValidation<>(InterfaceEntity.class);
        final MultiValidation<PublicPackageInterfaceEntity> mv3 = new MultiValidation<>(PublicPackageInterfaceEntity.class);

        assertEquals(mv1.hashCode(), mv2.hashCode());
        assertEquals(mv1, mv2);
        assertNotEquals(mv1.hashCode(), mv3.hashCode());
        assertNotEquals(mv1, mv3);

        final Validation<InterfaceEntity> validation = new AbstractValidation<InterfaceEntity>(InterfaceEntity.class) {
            @Override
            public void validate(InterfaceEntity entity, InterfaceEntity original, Validate validate) {
            }
        };

        mv1.addValidation(validation);
        assertNotEquals(mv1.hashCode(), mv2.hashCode());
        assertNotEquals(mv1, mv2);

        mv2.addValidation(validation);
        assertEquals(mv1.hashCode(), mv2.hashCode());
        assertEquals(mv1, mv2);
    }

    /**
     * 测试equals方法.
     */
    public void testEquals() {
        final MultiValidation<InterfaceEntity> mv1 = new MultiValidation<>(InterfaceEntity.class);
        assertNotEquals(mv1, null);
        assertNotEquals(mv1, new Object());
        assertEquals(mv1, mv1);
    }

    /**
     * 测试validate方法.
     */
    public void testValidate() {
        final MultiValidation<InterfaceEntity> mv = new MultiValidation<>(InterfaceEntity.class);
        final AtomicInteger calls = new AtomicInteger(0);
        final Validation<InterfaceEntity> falseValidation = new AbstractValidation<InterfaceEntity>(InterfaceEntity.class) {
            @Override
            public void validate(InterfaceEntity entity, InterfaceEntity original, Validate validate) {
                calls.incrementAndGet();
            }
        };
        final Validation<InterfaceEntity> trueValidation = new AbstractValidation<InterfaceEntity>(InterfaceEntity.class) {
            @Override
            public void validate(InterfaceEntity entity, InterfaceEntity original, Validate validate) {
                calls.incrementAndGet();
            }
        };
        final Validation<InterfaceEntity> multiValidation = new MultiValidation<>(InterfaceEntity.class, falseValidation);
        final InterfaceEntity entity = EntityFactory.newInstance(InterfaceEntity.class);

        assertEquals(0, mv.size());
        assertTrue(mv.isEmpty());
        mv.validate(entity, null, null);
        assertEquals(0, calls.get());

        mv.addValidation(falseValidation);
        assertEquals(1, mv.size());
        assertFalse(mv.isEmpty());
        mv.validate(entity, null, null);
        assertEquals(1, calls.get());

        mv.addValidation(trueValidation);
        mv.validate(entity, null, null);
        assertEquals(3, calls.get());

        mv.addValidation(trueValidation);
        assertEquals(2, mv.size());
        mv.validate(entity, null, null);
        assertEquals(5, calls.get());

        mv.addValidation(multiValidation);
        assertEquals(2, mv.size());
        mv.validate(entity, null, null);
        assertEquals(7, calls.get());
    }

    public void testToString() {
        assertEquals(MultiValidation.class.getName() + "<" + InterfaceEntity.class.getName() + "> [validations=0]",
                new MultiValidation<>(InterfaceEntity.class).toString());
    }
}
