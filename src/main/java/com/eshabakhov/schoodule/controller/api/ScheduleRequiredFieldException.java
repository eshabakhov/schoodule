/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.api;

/**
 * Schedule request required field exception.
 *
 * @since 0.0.1
 */
public final class ScheduleRequiredFieldException extends Exception {

    /**
     * New exception.
     *
     * @param message Error message
     */
    public ScheduleRequiredFieldException(final String message) {
        super(message);
    }
}
