/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.input;

/**
 * Invalid federal curriculum input.
 *
 * @since 0.0.1
 */
public final class CurriculumInputException extends Exception {

    /** Serialization identifier. */
    private static final long serialVersionUID = 1L;

    /**
     * New input exception.
     *
     * @param message Error message
     */
    public CurriculumInputException(final String message) {
        this(message, null);
    }

    /**
     * New input exception.
     *
     * @param message Error message
     * @param cause Error cause
     */
    public CurriculumInputException(
        final String message,
        final Throwable cause
    ) {
        super(message, cause);
    }
}
