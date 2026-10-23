/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.schedule.load.input;

/**
 * Invalid teaching load input.
 *
 * @since 0.0.1
 */
public final class LoadInputException extends Exception {

    /** Serialization identifier. */
    private static final long serialVersionUID = 1L;

    /**
     * New input exception.
     *
     * @param message Error message
     */
    public LoadInputException(final String message) {
        this(message, null);
    }

    /**
     * New input exception.
     *
     * @param message Error message
     * @param cause Error cause
     */
    public LoadInputException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
