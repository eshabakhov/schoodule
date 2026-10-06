/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.schedule.load;

/**
 * Class load was not found.
 *
 * @since 0.0.1
 */
public final class LoadNotFoundException extends Exception {

    /**
     * New exception.
     *
     * @param message Error message
     * @since 0.0.1
     */
    public LoadNotFoundException(final String message) {
        super(message);
    }
}
