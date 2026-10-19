/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.user;

/**
 * User not found exception.
 *
 * @since 0.0.1
 */
public final class UserNotFoundException extends Exception {

    /**
     * New exception.
     *
     * @param message Error message
     */
    public UserNotFoundException(final String message) {
        super(message);
    }
}
