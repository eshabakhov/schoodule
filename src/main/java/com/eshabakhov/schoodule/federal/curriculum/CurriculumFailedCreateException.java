/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum;

/**
 * Failure to create a federal curriculum.
 *
 * @since 0.0.1
 */
public final class CurriculumFailedCreateException extends Exception {

    /**
     * New exception.
     *
     * @since 0.0.1
     */
    public CurriculumFailedCreateException() {
        super("Failed to create federal curriculum");
    }
}
