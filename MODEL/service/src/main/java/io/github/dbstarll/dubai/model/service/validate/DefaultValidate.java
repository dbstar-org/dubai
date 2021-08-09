package io.github.dbstarll.dubai.model.service.validate;

import java.util.*;

public class DefaultValidate implements Validate {
    private Collection<String> actionErrors;
    private Map<String, List<String>> fieldErrors;

    @Override
    public Collection<String> getActionErrors() {
        return new LinkedList<String>(internalGetActionErrors());
    }

    @Override
    public Map<String, List<String>> getFieldErrors() {
        return new LinkedHashMap<String, List<String>>(internalGetFieldErrors());
    }

    @Override
    public void addActionError(String anErrorMessage) {
        final Collection<String> errors = internalGetActionErrors();
        if (!errors.contains(anErrorMessage)) {
            errors.add(anErrorMessage);
        }
    }

    @Override
    public void addFieldError(String fieldName, String errorMessage) {
        final Map<String, List<String>> errors = internalGetFieldErrors();
        List<String> thisFieldErrors = errors.get(fieldName);

        if (thisFieldErrors == null) {
            thisFieldErrors = new ArrayList<String>();
            errors.put(fieldName, thisFieldErrors);
        }

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
            actionErrors = new ArrayList<String>();
        }

        return actionErrors;
    }

    private Map<String, List<String>> internalGetFieldErrors() {
        if (fieldErrors == null) {
            fieldErrors = new LinkedHashMap<String, List<String>>();
        }

        return fieldErrors;
    }

}
