/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.api;

/**
 * Curriculum request required field exception.
 *
 * @since 0.0.1
 */
final class CurriculumRequiredFieldException extends Exception {

    /**
     * New exception.
     *
     * @param message Error message
     */
    CurriculumRequiredFieldException(final String message) {
        super(message);
    }
}
