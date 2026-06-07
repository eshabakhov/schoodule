/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.curriculum;

import com.eshabakhov.schoodule.Media;
import lombok.EqualsAndHashCode;

/**
 * Decorator that adds weekly hours to a {@link FederalCurriculumRequirement}.
 *
 * @since 0.0.1
 */
@EqualsAndHashCode(of = {"origin", "hours"})
public final class FcrWeekled implements FederalCurriculumRequirement {

    /**
     * Wrapped requirement.
     */
    private final FederalCurriculumRequirement origin;

    /**
     * Weekly hours.
     */
    private final Integer hours;

    /**
     * Creates a weekled requirement decorator.
     *
     * @param origin Wrapped requirement
     * @param hours  Weekly hours count
     */
    public FcrWeekled(final FederalCurriculumRequirement origin, final Integer hours) {
        this.origin = origin;
        this.hours = hours;
    }

    @Override
    public Long uid() {
        return this.origin.uid();
    }

    @Override
    public Media print(final Media media) {
        return this.origin.print(media).with("weeklyHours", this.hours);
    }

    @Override
    public FederalCurriculumRequirement regraded(final Integer grade) {
        return new FcrWeekled(this.origin.regraded(grade), this.hours);
    }

    @Override
    public FederalCurriculumRequirement resubjected(final String subject) {
        return new FcrWeekled(this.origin.resubjected(subject), this.hours);
    }

    @Override
    public FederalCurriculumRequirement reweekled(final Integer hrs) {
        return new FcrWeekled(this.origin, hrs);
    }

    @Override
    public FederalCurriculumRequirement reparted(final PartType part) {
        return new FcrWeekled(this.origin.reparted(part), this.hours);
    }
}
