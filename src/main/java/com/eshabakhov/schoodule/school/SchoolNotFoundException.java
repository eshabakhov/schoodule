/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school;

/**
 * School was not found.
 *
 * @since 0.0.1
 */
public final class SchoolNotFoundException extends Exception {

    /**
     * New exception.
     *
     * @param message Error message
     * @since 0.0.1
     */
    public SchoolNotFoundException(final String message) {
        super(message);
    }
}
