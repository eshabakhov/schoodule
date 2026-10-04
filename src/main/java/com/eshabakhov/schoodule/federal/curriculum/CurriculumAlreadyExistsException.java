/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum;

/**
 * Federal curriculum already exists.
 *
 * @since 0.0.1
 */
public final class CurriculumAlreadyExistsException extends Exception {

    /**
     * New exception.
     *
     * @param message Error message
     * @since 0.0.1
     */
    public CurriculumAlreadyExistsException(final String message) {
        super(message);
    }
}
