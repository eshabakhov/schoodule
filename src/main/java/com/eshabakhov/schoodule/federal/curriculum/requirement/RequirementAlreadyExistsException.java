/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement;

/**
 * Federal curriculum requirement already exists.
 *
 * @since 0.0.1
 */
public final class RequirementAlreadyExistsException extends Exception {

    /**
     * New exception.
     *
     * @since 0.0.1
     */
    public RequirementAlreadyExistsException() {
        super("FederalCurriculumRequirement already exists");
    }
}
