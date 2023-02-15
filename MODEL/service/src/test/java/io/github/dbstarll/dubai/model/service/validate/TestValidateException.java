package io.github.dbstarll.dubai.model.service.validate;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class TestValidateException {
    /**
     * 测试传入validate为null.
     */
    @Test
    void testNull() {
        final ValidateException exception = new ValidateException(null);
        assertEquals("[]", exception.getMessage());
        assertNull(exception.getValidate());
    }

    /**
     * 测试传入message且validate为null.
     */
    @Test
    void testNullMessage() {
        final ValidateException exception = new ValidateException("message", null);
        assertEquals("message", exception.getMessage());
        assertNull(exception.getValidate());
    }

    /**
     * 测试addActionError.
     */
    @Test
    void testAddActionError() {
        final Validate validate = new DefaultValidate();
        validate.addActionError("addActionError");

        final ValidateException exception = new ValidateException(validate);
        assertEquals("[addActionError]", exception.getMessage());
        assertSame(validate, exception.getValidate());
    }

    /**
     * 测试addFieldError.
     */
    @Test
    void testAddFieldError() {
        final Validate validate = new DefaultValidate();
        validate.addFieldError("f1", "addFieldError");
        validate.addFieldError("f1", "addFieldError1");
        final ValidateException exception = new ValidateException(validate);
        assertEquals("[f1=[addFieldError, addFieldError1]]", exception.getMessage());
        assertSame(validate, exception.getValidate());
    }
}
