package test.io.github.dbstarll.dubai.model.service.validate;

import io.github.dbstarll.dubai.model.service.validate.DefaultValidate;
import io.github.dbstarll.dubai.model.service.validate.Validate;
import junit.framework.TestCase;

public class TestDefaultValidate extends TestCase {
    /**
     * 测试初始状态.
     */
    public void testInit() {
        final Validate validate = new DefaultValidate();

        assertFalse(validate.hasErrors());
        assertFalse(validate.hasActionErrors());
        assertFalse(validate.hasFieldErrors());

        assertTrue(validate.getFieldErrors().isEmpty());
        assertTrue(validate.getActionErrors().isEmpty());
    }

    /**
     * 测试addActionError.
     */
    public void testAddActionError() {
        final Validate validate = new DefaultValidate();
        assertFalse(validate.hasErrors());
        assertFalse(validate.hasActionErrors());
        assertFalse(validate.hasFieldErrors());

        assertTrue(validate.getFieldErrors().isEmpty());
        assertTrue(validate.getActionErrors().isEmpty());

        validate.addActionError("addActionError");

        assertTrue(validate.hasErrors());
        assertTrue(validate.hasActionErrors());
        assertFalse(validate.hasFieldErrors());

        assertTrue(validate.getFieldErrors().isEmpty());
        assertFalse(validate.getActionErrors().isEmpty());

        assertEquals(1, validate.getActionErrors().size());
        assertEquals("[addActionError]", validate.getActionErrors().toString());

        validate.addActionError("addActionError");

        assertEquals(1, validate.getActionErrors().size());
        assertEquals("[addActionError]", validate.getActionErrors().toString());

        validate.addActionError("addActionError1");

        assertEquals(2, validate.getActionErrors().size());
        assertEquals("[addActionError, addActionError1]", validate.getActionErrors().toString());
    }

    /**
     * 测试addFieldError.
     */
    public void testAddFieldError() {
        final Validate validate = new DefaultValidate();
        assertFalse(validate.hasErrors());
        assertFalse(validate.hasActionErrors());
        assertFalse(validate.hasFieldErrors());

        assertTrue(validate.getFieldErrors().isEmpty());
        assertTrue(validate.getActionErrors().isEmpty());

        validate.addFieldError("f1", "addFieldError");

        assertTrue(validate.hasErrors());
        assertFalse(validate.hasActionErrors());
        assertTrue(validate.hasFieldErrors());

        assertFalse(validate.getFieldErrors().isEmpty());
        assertTrue(validate.getActionErrors().isEmpty());

        assertEquals(1, validate.getFieldErrors().size());
        assertEquals(1, validate.getFieldErrors().get("f1").size());
        assertEquals("[addFieldError]", validate.getFieldErrors().get("f1").toString());

        validate.addFieldError("f1", "addFieldError");

        assertEquals(1, validate.getFieldErrors().size());
        assertEquals(1, validate.getFieldErrors().get("f1").size());
        assertEquals("[addFieldError]", validate.getFieldErrors().get("f1").toString());

        validate.addFieldError("f1", "addFieldError1");

        assertEquals(1, validate.getFieldErrors().size());
        assertEquals(2, validate.getFieldErrors().get("f1").size());
        assertEquals("[addFieldError, addFieldError1]", validate.getFieldErrors().get("f1").toString());
    }
}
