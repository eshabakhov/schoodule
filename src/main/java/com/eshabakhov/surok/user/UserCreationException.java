/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.user;

/**
 * User creation exception.
 *
 * @since 0.0.1
 */
public final class UserCreationException extends Exception {

    /**
     * New exception.
     *
     * @param message Error message
     */
    public UserCreationException(final String message) {
        super(message);
    }
}
