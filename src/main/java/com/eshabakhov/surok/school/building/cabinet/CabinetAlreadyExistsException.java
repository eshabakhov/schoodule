/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.building.cabinet;

/**
 * Cabinet already exists.
 *
 * @since 0.0.1
 */
public final class CabinetAlreadyExistsException extends Exception {

    /**
     * New exception.
     *
     * @param message Error message
     * @since 0.0.1
     */
    public CabinetAlreadyExistsException(final String message) {
        super(message);
    }
}
