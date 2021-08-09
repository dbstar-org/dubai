package io.github.dbstarll.dubai.model.service.validate;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface Validate {
    /**
     * Get the Collection of Action-level error messages for this action. Error messages should not be
     * added directly here, as implementations are free to return a new Collection or an Unmodifiable
     * Collection.
     *
     * @return Collection of String error messages
     */
    Collection<String> getActionErrors();

    /**
     * Get the field specific errors associated with this action. Error messages should not be added
     * directly here, as implementations are free to return a new Collection or an Unmodifiable
     * Collection.
     *
     * @return Map with errors mapped from fieldname (String) to Collection of String error messages
     */
    Map<String, List<String>> getFieldErrors();

    /**
     * Add an Action-level error message to this Action.
     *
     * @param anErrorMessage the error message
     */
    void addActionError(String anErrorMessage);

    /**
     * Add an error message for a given field.
     *
     * @param fieldName    name of field
     * @param errorMessage the error message
     */
    void addFieldError(String fieldName, String errorMessage);

    /**
     * Check whether there are any Action-level error messages.
     *
     * @return true if any Action-level error messages have been registered
     */
    boolean hasActionErrors();

    /**
     * Checks whether there are any action errors or field errors.
     *
     * <b>Note</b>: that this does not have the same meaning as in WW 1.x.
     *
     * @return <code>(hasActionErrors() || hasFieldErrors())</code>
     */
    boolean hasErrors();

    /**
     * Check whether there are any field errors associated with this action.
     *
     * @return whether there are any field errors
     */
    boolean hasFieldErrors();
}
