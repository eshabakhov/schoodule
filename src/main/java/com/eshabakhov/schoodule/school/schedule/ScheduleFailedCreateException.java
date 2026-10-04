/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.schedule;

/**
 * Failure to create a schedule.
 *
 * @since 0.0.1
 */
public final class ScheduleFailedCreateException extends Exception {

    /**
     * New exception.
     *
     * @since 0.0.1
     */
    public ScheduleFailedCreateException() {
        super("Failed to create Schedule");
    }
}
