/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user;

/**
 * Role assignment exception.
 *
 * @since 0.0.1
 */
public final class RoleAssignmentException extends Exception {

    /**
     * New exception.
     *
     * @param message Error message
     */
    public RoleAssignmentException(final String message) {
        super(message);
    }
}
