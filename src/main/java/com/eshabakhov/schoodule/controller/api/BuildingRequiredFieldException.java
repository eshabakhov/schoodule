/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.api;

/**
 * Building request required field exception.
 *
 * @since 0.0.1
 */
final class BuildingRequiredFieldException extends Exception {

    /**
     * New exception.
     *
     * @param message Error message
     */
    BuildingRequiredFieldException(final String message) {
        super(message);
    }
}
