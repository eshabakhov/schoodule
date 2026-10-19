/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.controller.api;

/**
 * Cabinet request required field exception.
 *
 * @since 0.0.1
 */
public final class CabinetRequiredFieldException extends Exception {

    /**
     * New exception.
     *
     * @param message Error message
     */
    public CabinetRequiredFieldException(final String message) {
        super(message);
    }
}
