/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement;

/**
 * Federal curriculum requirement was not found.
 *
 * @since 0.0.1
 */
public final class RequirementNotFoundException extends Exception {

    /**
     * New exception.
     *
     * @param message Error message
     * @since 0.0.1
     */
    public RequirementNotFoundException(final String message) {
        super(message);
    }
}
