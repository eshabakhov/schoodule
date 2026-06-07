/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.curriculum;

import com.eshabakhov.schoodule.Media;
import lombok.EqualsAndHashCode;

/**
 * Decorator that adds a subject name to a {@link FederalCurriculumRequirement}.
 *
 * @since 0.0.1
 */
@EqualsAndHashCode(of = {"origin", "subject"})
public final class FcrSubjected implements FederalCurriculumRequirement {

    /**
     * Wrapped requirement.
     */
    private final FederalCurriculumRequirement origin;

    /**
     * Subject name.
     */
    private final String subject;

    /**
     * Creates a subjected requirement decorator.
     *
     * @param origin  Wrapped requirement
     * @param subject Subject name
     */
    public FcrSubjected(final FederalCurriculumRequirement origin, final String subject) {
        this.origin = origin;
        this.subject = subject;
    }

    @Override
    public Long uid() {
        return this.origin.uid();
    }

    @Override
    public Media print(final Media media) {
        return this.origin.print(media).with("subjectName", this.subject);
    }

    @Override
    public FederalCurriculumRequirement regraded(final Integer grade) {
        return new FcrSubjected(this.origin.regraded(grade), this.subject);
    }

    @Override
    public FederalCurriculumRequirement resubjected(final String subj) {
        return new FcrSubjected(this.origin, subj);
    }

    @Override
    public FederalCurriculumRequirement reweekled(final Integer hours) {
        return new FcrWeekled(this, hours);
    }

    @Override
    public FederalCurriculumRequirement reparted(final PartType part) {
        return new FcrSubjected(this.origin.reparted(part), this.subject);
    }
}
