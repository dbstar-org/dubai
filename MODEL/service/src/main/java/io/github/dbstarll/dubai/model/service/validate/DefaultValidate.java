package io.github.dbstarll.dubai.model.service.validate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DefaultValidate implements Validate {
    private Collection<String> actionErrors;
    private Map<String, List<String>> fieldErrors;

    @Override
    public Collection<String> getActionErrors() {
        return new LinkedList<>(internalGetActionErrors());
    }

    @Override
    public Map<String, List<String>> getFieldErrors() {
        return new LinkedHashMap<>(internalGetFieldErrors());
    }

    @Override
    public void addActionError(final String anErrorMessage) {
        final Collection<String> errors = internalGetActionErrors();
        if (!errors.contains(anErrorMessage)) {
            errors.add(anErrorMessage);
        }
    }

    @Override
    public void addFieldError(final String fieldName, final String errorMessage) {
        final Map<String, List<String>> errors = internalGetFieldErrors();
        List<String> thisFieldErrors = errors.computeIfAbsent(fieldName, k -> new ArrayList<>());

        if (!thisFieldErrors.contains(errorMessage)) {
            thisFieldErrors.add(errorMessage);
        }
    }

    @Override
    public boolean hasActionErrors() {
        return actionErrors != null && !actionErrors.isEmpty();
    }

    @Override
    public boolean hasErrors() {
        return hasActionErrors() || hasFieldErrors();
    }

    @Override
    public boolean hasFieldErrors() {
        return fieldErrors != null && !fieldErrors.isEmpty();
    }

    private Collection<String> internalGetActionErrors() {
        if (actionErrors == null) {
            actionErrors = new ArrayList<>();
        }

        return actionErrors;
    }

    private Map<String, List<String>> internalGetFieldErrors() {
        if (fieldErrors == null) {
            fieldErrors = new LinkedHashMap<>();
        }

        return fieldErrors;
    }

}
