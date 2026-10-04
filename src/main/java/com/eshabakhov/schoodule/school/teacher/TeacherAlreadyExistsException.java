/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.teacher;

/**
 * Teacher already exists.
 *
 * @since 0.0.1
 */
public final class TeacherAlreadyExistsException extends Exception {

    /**
     * New exception.
     *
     * @param message Error message
     * @since 0.0.1
     */
    public TeacherAlreadyExistsException(final String message) {
        super(message);
    }
}
