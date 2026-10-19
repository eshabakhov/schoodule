/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.federal.curriculum.requirement.input;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.federal.curriculum.requirement.RequirementInput;

/**
 * Federal curriculum requirement input with a grade.
 *
 * @since 0.0.1
 */
public final class RqInputGrade implements RequirementInput {

    /** Origin input. */
    private final RequirementInput origin;

    /** Requirement grade. */
    private final String grade;

    /**
     * New grade input.
     *
     * @param origin Origin input
     * @param grade Requirement grade
     */
    public RqInputGrade(final RequirementInput origin, final String grade) {
        this.origin = origin;
        this.grade = grade;
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        if (this.grade == null || this.grade.isBlank()) {
            throw new RequirementInputException(
                "Requirement grade is required"
            );
        }
        try {
            return this.origin.print(media).with(
                "grade", Integer.valueOf(this.grade.trim())
            );
        } catch (final NumberFormatException exception) {
            throw new RequirementInputException(
                String.format("Invalid requirement grade `%s`", this.grade),
                exception
            );
        }
    }
}
