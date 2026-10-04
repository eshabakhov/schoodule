/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.api;

/**
 * School class request required field exception.
 *
 * @since 0.0.1
 */
public final class SchoolClassRequiredFieldException extends Exception {

    /**
     * New exception.
     *
     * @param message Error message
     */
    public SchoolClassRequiredFieldException(final String message) {
        super(message);
    }
}
