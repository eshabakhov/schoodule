/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.schoolclass;

/**
 * School class was not found.
 *
 * @since 0.0.1
 */
public final class SchoolClassNotFoundException extends Exception {

    /**
     * New exception.
     *
     * @param message Error message
     * @since 0.0.1
     */
    public SchoolClassNotFoundException(final String message) {
        super(message);
    }
}
