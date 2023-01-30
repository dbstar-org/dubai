package io.github.dbstarll.dubai.model.service.validate;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

public class ValidateException extends RuntimeException {
    private static final long serialVersionUID = -740523588382527452L;

    private final transient Validate validate;

    /**
     * 构造ValidateException.
     *
     * @param validate Validate实例
     */
    public ValidateException(final Validate validate) {
        super(message(validate));
        this.validate = validate;
    }

    /**
     * 构造ValidateException.
     *
     * @param message  异常消息
     * @param validate Validate实例
     */
    public ValidateException(final String message, final Validate validate) {
        super(message);
        this.validate = validate;
    }

    /**
     * 获得Validate实例.
     *
     * @return Validate实例
     */
    public Validate getValidate() {
        return validate;
    }

    private static String message(final Validate validate) {
        final Set<String> messages = new TreeSet<>();
        if (validate != null) {
            if (validate.hasActionErrors()) {
                messages.addAll(validate.getActionErrors());
            }
            if (validate.hasFieldErrors()) {
                for (Entry<String, List<String>> entry : validate.getFieldErrors().entrySet()) {
                    messages.add(entry.toString());
                }
            }
        }
        return messages.toString();
    }
}
