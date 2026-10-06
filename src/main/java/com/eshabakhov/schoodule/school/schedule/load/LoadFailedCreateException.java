/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.schedule.load;

/**
 * Failure to create a class load.
 *
 * @since 0.0.1
 */
public final class LoadFailedCreateException extends Exception {

    /**
     * New exception.
     *
     * @since 0.0.1
     */
    public LoadFailedCreateException() {
        super("Failed to create class load");
    }
}
