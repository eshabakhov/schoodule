/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user;

/**
 * Registration exception.
 *
 * @since 0.0.1
 */
public final class RegistrationException extends Exception {

    /**
     * New exception.
     *
     * @param message Error message
     */
    public RegistrationException(final String message) {
        super(message);
    }
}
