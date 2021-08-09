package test.io.github.dbstarll.dubai.model.service.validate;

import io.github.dbstarll.dubai.model.service.validate.DefaultValidate;
import io.github.dbstarll.dubai.model.service.validate.Validate;
import io.github.dbstarll.dubai.model.service.validate.ValidateException;
import junit.framework.TestCase;

public class TestValidateException extends TestCase {
    /**
     * 测试传入validate为null.
     */
    public void testNull() {
        final ValidateException exception = new ValidateException(null);
        assertEquals("[]", exception.getMessage());
        assertNull(exception.getValidate());
    }

    /**
     * 测试传入message且validate为null.
     */
    public void testNullMessage() {
        final ValidateException exception = new ValidateException("message", null);
        assertEquals("message", exception.getMessage());
        assertNull(exception.getValidate());
    }

    /**
     * 测试addActionError.
     */
    public void testAddActionError() {
        final Validate validate = new DefaultValidate();
        validate.addActionError("addActionError");

        final ValidateException exception = new ValidateException(validate);
        assertEquals("[addActionError]", exception.getMessage());
        assertSame(validate, exception.getValidate());
    }

    /**
     * 测试addFieldError.
     */
    public void testAddFieldError() {
        final Validate validate = new DefaultValidate();
        validate.addFieldError("f1", "addFieldError");
        validate.addFieldError("f1", "addFieldError1");
        final ValidateException exception = new ValidateException(validate);
        assertEquals("[f1=[addFieldError, addFieldError1]]", exception.getMessage());
        assertSame(validate, exception.getValidate());
    }
}
