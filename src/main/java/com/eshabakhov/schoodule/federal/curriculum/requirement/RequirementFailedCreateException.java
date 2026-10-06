/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement;

/**
 * Failure to create a federal curriculum requirement.
 *
 * @since 0.0.1
 */
public final class RequirementFailedCreateException extends Exception {

    /**
     * New exception.
     *
     * @since 0.0.1
     */
    public RequirementFailedCreateException() {
        super("Failed to create federal curriculum requirement");
    }
}
