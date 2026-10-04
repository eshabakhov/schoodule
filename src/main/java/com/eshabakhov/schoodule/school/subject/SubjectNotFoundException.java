/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.subject;

/**
 * Subject was not found.
 *
 * @since 0.0.1
 */
public final class SubjectNotFoundException extends Exception {

    /**
     * New exception.
     *
     * @param message Error message
     * @since 0.0.1
     */
    public SubjectNotFoundException(final String message) {
        super(message);
    }
}
