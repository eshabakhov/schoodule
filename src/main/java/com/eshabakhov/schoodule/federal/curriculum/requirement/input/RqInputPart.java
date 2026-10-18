/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement.input;

import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.federal.curriculum.requirement.Requirement;
import com.eshabakhov.schoodule.federal.curriculum.requirement.RequirementInput;

/**
 * Federal curriculum requirement input with a curriculum part.
 *
 * @since 0.0.1
 */
public final class RqInputPart implements RequirementInput {

    /** Origin input. */
    private final RequirementInput origin;

    /** Curriculum part. */
    private final String part;

    /**
     * New curriculum-part input.
     *
     * @param origin Origin input
     * @param part Curriculum part
     */
    public RqInputPart(final RequirementInput origin, final String part) {
        this.origin = origin;
        this.part = part;
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        if (this.part == null || this.part.isBlank()) {
            throw new RequirementInputException(
                "Requirement curriculum part is required"
            );
        }
        try {
            return this.origin.print(media).with(
                "partType",
                Requirement.PartType.valueOf(this.part.trim()).name()
            );
        } catch (final IllegalArgumentException exception) {
            throw new RequirementInputException(
                String.format(
                    "Unsupported requirement curriculum part `%s`", this.part
                ),
                exception
            );
        }
    }
}
