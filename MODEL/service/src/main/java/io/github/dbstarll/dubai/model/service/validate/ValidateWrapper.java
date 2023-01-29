package io.github.dbstarll.dubai.model.service.validate;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class ValidateWrapper extends DefaultValidate {
    private final Validate validate;

    private ValidateWrapper(final Validate validate) {
        this.validate = validate;
    }

    @Override
    public Collection<String> getActionErrors() {
        return validate != null ? validate.getActionErrors() : super.getActionErrors();
    }

    @Override
    public Map<String, List<String>> getFieldErrors() {
        return validate != null ? validate.getFieldErrors() : super.getFieldErrors();
    }

    @Override
    public void addActionError(final String anErrorMessage) {
        if (validate != null) {
            validate.addActionError(anErrorMessage);
        } else {
            super.addActionError(anErrorMessage);
        }
    }

    @Override
    public void addFieldError(final String fieldName, final String errorMessage) {
        if (validate != null) {
            validate.addFieldError(fieldName, errorMessage);
        } else {
            super.addFieldError(fieldName, errorMessage);
        }
    }

    @Override
    public boolean hasActionErrors() {
        return validate != null ? validate.hasActionErrors() : super.hasActionErrors();
    }

    @Override
    public boolean hasErrors() {
        return validate != null ? validate.hasErrors() : super.hasErrors();
    }

    @Override
    public boolean hasFieldErrors() {
        return validate != null ? validate.hasFieldErrors() : super.hasFieldErrors();
    }

    /**
     * 封装一个Validate实例.
     *
     * @param validate 被封装的Validate实例
     * @return 封装后的Validate实例
     */
    public static Validate wrap(final Validate validate) {
        return new ValidateWrapper(validate);
    }
}
