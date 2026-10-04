/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user;

/**
 * Role not found exception.
 *
 * @since 0.0.1
 */
public final class RoleNotFoundException extends Exception {

    /**
     * New exception.
     *
     * @param message Error message
     */
    public RoleNotFoundException(final String message) {
        super(message);
    }
}
