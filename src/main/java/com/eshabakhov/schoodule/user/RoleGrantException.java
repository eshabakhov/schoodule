/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user;

/**
 * Role grant exception.
 *
 * @since 0.0.1
 */
public final class RoleGrantException extends Exception {

    /**
     * New exception.
     *
     * @param message Error message
     */
    public RoleGrantException(final String message) {
        super(message);
    }
}
