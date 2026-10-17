/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement.input;

import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.federal.curriculum.requirement.RequirementInput;

/**
 * Federal curriculum requirement input with a subject.
 *
 * @since 0.1.0
 */
public final class RqInputSubject implements RequirementInput {

    /** Origin input. */
    private final RequirementInput origin;

    /** Requirement subject. */
    private final String subject;

    /**
     * New subject input.
     *
     * @param origin Origin input
     * @param subject Requirement subject
     */
    public RqInputSubject(
        final RequirementInput origin,
        final String subject
    ) {
        this.origin = origin;
        this.subject = subject;
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        if (this.subject == null || this.subject.isBlank()) {
            throw new RequirementInputException(
                "Requirement subject is required"
            );
        }
        return this.origin.print(media).with(
            "subjectName", this.subject.trim()
        );
    }
}
