package io.github.dbstarll.dubai.model.service.validate;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

public class ValidateException extends RuntimeException {
    private static final long serialVersionUID = -740523588382527452L;

    private final transient Validate validate;

    public ValidateException(Validate validate) {
        super(message(validate));
        this.validate = validate;
    }

    public ValidateException(String message, Validate validate) {
        super(message);
        this.validate = validate;
    }

    public Validate getValidate() {
        return validate;
    }

    private static String message(Validate validate) {
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
