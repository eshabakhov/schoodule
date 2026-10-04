/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.teacher;

/**
 * Failure to create a teacher.
 *
 * @since 0.0.1
 */
public final class TeacherFailedCreateException extends Exception {

    /**
     * New exception.
     *
     * @since 0.0.1
     */
    public TeacherFailedCreateException() {
        super("Failed to create Teacher");
    }
}
