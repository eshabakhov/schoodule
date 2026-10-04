/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.schoolclass;

/**
 * Failure to create a school class.
 *
 * @since 0.0.1
 */
public final class SchoolClassFailedCreateException extends Exception {

    /**
     * New exception.
     *
     * @since 0.0.1
     */
    public SchoolClassFailedCreateException() {
        super("Failed to create SchoolClass");
    }
}
