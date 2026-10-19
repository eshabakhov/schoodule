/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.schedule.load;

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
