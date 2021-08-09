package io.github.dbstarll.dubai.model.service.validate;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ValidateWrapper extends DefaultValidate {
    private final Validate validate;

    private ValidateWrapper(Validate validate) {
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
    public void addActionError(String anErrorMessage) {
        if (validate != null) {
            validate.addActionError(anErrorMessage);
        } else {
            super.addActionError(anErrorMessage);
        }
    }

    @Override
    public void addFieldError(String fieldName, String errorMessage) {
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

    public static Validate wrap(Validate validate) {
        return new ValidateWrapper(validate);
    }
}
