/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.building.cabinet;

/**
 * Cabinet was not found.
 *
 * @since 0.0.1
 */
public final class CabinetNotFoundException extends Exception {

    /**
     * New exception.
     *
     * @param message Error message
     * @since 0.0.1
     */
    public CabinetNotFoundException(final String message) {
        super(message);
    }
}
