/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.schedule;

/**
 * Schedule was not found.
 *
 * @since 0.0.1
 */
public final class ScheduleNotFoundException extends Exception {

    /**
     * New exception.
     *
     * @param message Error message
     * @since 0.0.1
     */
    public ScheduleNotFoundException(final String message) {
        super(message);
    }
}
