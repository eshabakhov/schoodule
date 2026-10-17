/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement.input;

import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.federal.curriculum.requirement.RequirementInput;

/**
 * Federal curriculum requirement input with weekly hours.
 *
 * @since 0.1.0
 */
public final class RqInputHours implements RequirementInput {

    /** Origin input. */
    private final RequirementInput origin;

    /** Weekly hours. */
    private final String hours;

    /**
     * New weekly-hours input.
     *
     * @param origin Origin input
     * @param hours Weekly hours
     */
    public RqInputHours(final RequirementInput origin, final String hours) {
        this.origin = origin;
        this.hours = hours;
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        if (this.hours == null || this.hours.isBlank()) {
            throw new RequirementInputException(
                "Requirement weekly hours are required"
            );
        }
        try {
            return this.origin.print(media).with(
                "weeklyHours", Integer.valueOf(this.hours.trim())
            );
        } catch (final NumberFormatException exception) {
            throw new RequirementInputException(
                String.format(
                    "Invalid requirement weekly hours `%s`", this.hours
                ),
                exception
            );
        }
    }
}
