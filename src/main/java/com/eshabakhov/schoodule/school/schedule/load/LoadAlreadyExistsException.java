/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.schedule.load;

/**
 * Class load already exists.
 *
 * @since 0.0.1
 */
public final class LoadAlreadyExistsException extends Exception {

    /**
     * New exception.
     *
     * @since 0.0.1
     */
    public LoadAlreadyExistsException() {
        super("Class load already exists");
    }
}
