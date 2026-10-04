/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.schoolclass;

/**
 * School class already exists.
 *
 * @since 0.0.1
 */
public final class SchoolClassAlreadyExistsException extends Exception {

    /**
     * New exception.
     *
     * @param message Error message
     * @since 0.0.1
     */
    public SchoolClassAlreadyExistsException(final String message) {
        super(message);
    }
}
