/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.teacher;

/**
 * Teacher was not found.
 *
 * @since 0.0.1
 */
public final class TeacherNotFoundException extends Exception {

    /**
     * New exception.
     *
     * @param message Error message
     * @since 0.0.1
     */
    public TeacherNotFoundException(final String message) {
        super(message);
    }
}
