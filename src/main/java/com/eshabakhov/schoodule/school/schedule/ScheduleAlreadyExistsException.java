/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.schedule;

/**
 * Schedule already exists.
 *
 * @since 0.0.1
 */
public final class ScheduleAlreadyExistsException extends Exception {

    /**
     * New exception.
     *
     * @param message Error message
     * @since 0.0.1
     */
    public ScheduleAlreadyExistsException(final String message) {
        super(message);
    }
}
