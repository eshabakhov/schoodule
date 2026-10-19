/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.federal.curriculum.requirement.input;

/**
 * Invalid federal curriculum requirement input.
 *
 * @since 0.0.1
 */
public final class RequirementInputException extends Exception {

    /** Serialization identifier. */
    private static final long serialVersionUID = 1L;

    /**
     * New input exception.
     *
     * @param message Error message
     */
    public RequirementInputException(final String message) {
        this(message, null);
    }

    /**
     * New input exception.
     *
     * @param message Error message
     * @param cause Error cause
     */
    public RequirementInputException(
        final String message,
        final Throwable cause
    ) {
        super(message, cause);
    }
}
