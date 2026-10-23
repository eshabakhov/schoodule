/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.schoolclass.input;

/**
 * Invalid school class input.
 *
 * @since 0.0.1
 */
public final class SchoolClassInputException extends Exception {

    /** Serialization identifier. */
    private static final long serialVersionUID = 1L;

    /**
     * New input exception.
     *
     * @param message Error message
     */
    public SchoolClassInputException(final String message) {
        this(message, null);
    }

    /**
     * New input exception.
     *
     * @param message Error message
     * @param cause Error cause
     */
    public SchoolClassInputException(
        final String message,
        final Throwable cause
    ) {
        super(message, cause);
    }
}
