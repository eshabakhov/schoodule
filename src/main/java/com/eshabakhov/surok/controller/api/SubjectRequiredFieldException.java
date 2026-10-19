/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.controller.api;

/**
 * Subject request required field exception.
 *
 * @since 0.0.1
 */
public final class SubjectRequiredFieldException extends Exception {

    /**
     * New exception.
     *
     * @param message Error message
     */
    public SubjectRequiredFieldException(final String message) {
        super(message);
    }
}
