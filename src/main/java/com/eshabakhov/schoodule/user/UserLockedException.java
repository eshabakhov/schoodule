/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user;

/**
 * User locked exception.
 *
 * @since 0.0.1
 */
public final class UserLockedException extends Exception {

    /**
     * New exception.
     *
     * @param message Error message
     */
    public UserLockedException(final String message) {
        super(message);
    }
}
