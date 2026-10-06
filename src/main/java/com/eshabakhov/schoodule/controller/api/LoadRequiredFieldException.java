/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.api;

/**
 * Class load request required field exception.
 *
 * @since 0.0.1
 */
final class LoadRequiredFieldException extends Exception {

    /**
     * New exception.
     *
     * @param message Error message
     */
    LoadRequiredFieldException(final String message) {
        super(message);
    }
}
