/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.api;

/**
 * Teacher request required field exception.
 *
 * @since 0.0.1
 */
public final class TeacherRequiredFieldException extends Exception {

    /**
     * New exception.
     *
     * @param message Error message
     */
    public TeacherRequiredFieldException(final String message) {
        super(message);
    }
}
