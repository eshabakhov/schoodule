/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.api;

/**
 * School request required field exception.
 *
 * @since 0.0.1
 */
public final class SchoolRequiredFieldException extends Exception {

    /**
     * New exception.
     *
     * @param message Error message
     */
    public SchoolRequiredFieldException(final String message) {
        super(message);
    }
}
