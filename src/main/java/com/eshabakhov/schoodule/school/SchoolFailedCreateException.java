/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school;

/**
 * Failure to create a school.
 *
 * @since 0.0.1
 */
public final class SchoolFailedCreateException extends Exception {

    /**
     * New exception.
     *
     * @since 0.0.1
     */
    public SchoolFailedCreateException() {
        super("Failed to create School");
    }
}
