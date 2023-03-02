package io.github.dbstarll.dubai.model.entity.utils;

import io.github.dbstarll.dubai.model.entity.EnumValue;
import io.github.dbstarll.dubai.model.entity.test.enums.Custom;
import io.github.dbstarll.dubai.model.entity.test.enums.Default;
import io.github.dbstarll.dubai.model.entity.test.enums.Normal;
import io.github.dbstarll.dubai.model.entity.test.enums.ToString;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

class EnumValueHelperTest {
    enum NoPublicClass {
        ABC, DEF
    }

    @EnumValue(method = "abc")
    public enum NoPublicMethod {
        ABC, DEF;

        String abc() {
            return name();
        }
    }

    @EnumValue(method = "abc")
    public enum Static {
        ABC, DEF;

        public static String abc() {
            return "abc";
        }
    }

    @EnumValue(method = "")
    public enum Empty {
        ABC, DEF
    }

    @EnumValue(method = "abc")
    public enum ReturnNotString {
        ABC, DEF;

        public boolean abc() {
            return true;
        }
    }

    @EnumValue(method = "abc")
    public enum Duplicate {
        ABC, DEF;

        public String abc() {
            return "abc";
        }
    }

    @EnumValue(method = "abc")
    public enum Failed {
        ABC, DEF;

        public String abc() {
            throw new IllegalArgumentException("abc");
        }
    }

    private <T extends Enum<T>> void testEnum(final Class<T> enumClass, final Function<T, String> value) {
        final EnumValueHelper<T> helper = new EnumValueHelper<>(enumClass);
        Arrays.stream(enumClass.getEnumConstants()).forEach(e -> {
            assertEquals(value.apply(e), helper.name(e), "name() failed on " + enumClass.getName());
            assertSame(e, helper.valueOf(value.apply(e)), "valueOf() failed on " + enumClass.getName());
        });
    }

    @Test
    void helper() {
        testEnum(Normal.class, Enum::name);
        testEnum(Default.class, Enum::name);
        testEnum(Custom.class, Custom::getTitle);
        testEnum(ToString.class, Enum::toString);
    }

    @Test
    void noPublicClass() {
        final Exception e = assertThrowsExactly(IllegalArgumentException.class, () -> new EnumValueHelper<>(NoPublicClass.class));
        assertEquals("enumType must be public: " + NoPublicClass.class.getName(), e.getMessage());
        assertNull(e.getCause());
    }

    @Test
    void noPublicMethod() {
        final Exception e = assertThrowsExactly(IllegalArgumentException.class, () -> new EnumValueHelper<>(NoPublicMethod.class));
        assertEquals("get method[abc] failed: " + NoPublicMethod.class.getName(), e.getMessage());
        assertNotNull(e.getCause());
        assertSame(NoSuchMethodException.class, e.getCause().getClass());
        assertEquals(NoPublicMethod.class.getName() + ".abc()", e.getCause().getMessage());
    }

    @Test
    void Static() {
        final Exception e = assertThrowsExactly(IllegalArgumentException.class, () -> new EnumValueHelper<>(Static.class));
        assertEquals("method[abc] must not be static: " + Static.class.getName(), e.getMessage());
        assertNull(e.getCause());
    }

    @Test
    void empty() {
        final Exception e = assertThrowsExactly(IllegalArgumentException.class, () -> new EnumValueHelper<>(Empty.class));
        assertEquals("@EnumValue.method() not set: " + Empty.class.getName(), e.getMessage());
        assertNull(e.getCause());
    }

    @Test
    void returnNotString() {
        final Exception e = assertThrowsExactly(IllegalArgumentException.class, () -> new EnumValueHelper<>(ReturnNotString.class));
        assertEquals("method[abc]'s returnType must be String: " + ReturnNotString.class.getName(), e.getMessage());
        assertNull(e.getCause());
    }

    @Test
    void duplicate() {
        final Exception e = assertThrowsExactly(IllegalArgumentException.class, () -> new EnumValueHelper<>(Duplicate.class));
        assertEquals("duplicate name [abc] for [ABC] and [DEF]: " + Duplicate.class.getName(), e.getMessage());
        assertNull(e.getCause());
    }

    @Test
    void failed() {
        final Exception e = assertThrowsExactly(IllegalStateException.class, () -> new EnumValueHelper<>(Failed.class));
        assertEquals("get enum[ABC]'s name failed: " + Failed.class.getName(), e.getMessage());
        assertNotNull(e.getCause());
        assertSame(IllegalArgumentException.class, e.getCause().getClass());
        assertEquals("abc", e.getCause().getMessage());
    }

    @Test
    void nameNull() {
        final EnumValueHelper<Normal> helper = new EnumValueHelper<>(Normal.class);
        final Exception e = assertThrowsExactly(IllegalStateException.class, () -> helper.name(null));
        assertEquals("get enum[null]'s name failed: " + Normal.class.getName(), e.getMessage());
        assertNotNull(e.getCause());
        assertSame(NullPointerException.class, e.getCause().getClass());
    }

    @Test
    void valueOfNull() {
        final EnumValueHelper<Normal> helper = new EnumValueHelper<>(Normal.class);
        final Exception e = assertThrowsExactly(NullPointerException.class, () -> helper.valueOf(null));
        assertEquals("name is blank", e.getMessage());
    }

    @Test
    void valueOfUnknown() {
        final EnumValueHelper<Normal> helper = new EnumValueHelper<>(Normal.class);
        final Exception e = assertThrowsExactly(IllegalArgumentException.class, () -> helper.valueOf("unknown"));
        assertEquals("No enum constant[unknown]: " + Normal.class.getName(), e.getMessage());
    }
}