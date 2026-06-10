/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement;

import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.federal.curriculum.FederalCurriculumRequirement;

/**
 * Simple implementation of {@link FederalCurriculumRequirement}.
 *
 * @since 0.0.1
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class FcrSimple implements FederalCurriculumRequirement {

    /**
     * Original federal curriculum requirement.
     */
    private final FederalCurriculumRequirement origin;

    /**
     * Creates a Postgres-backed requirement.
     *
     * @param origin Original federal curriculum requirement.
     */
    public FcrSimple(final FederalCurriculumRequirement origin) {
        this.origin = origin;
    }

    @Override
    public Long uid() {
        return this.origin.uid();
    }

    @Override
    public Media print(final Media media) {
        return this.origin.print(media)
            .include("id", "grade", "subjectName", "weeklyHours", "partType");
    }

    @Override
    public FederalCurriculumRequirement regraded(final Integer grade) {
        return this.origin.regraded(grade);
    }

    @Override
    public FederalCurriculumRequirement resubjected(final String subject) {
        return this.origin.resubjected(subject);
    }

    @Override
    public FederalCurriculumRequirement reweekled(final Integer hours) {
        return this.origin.reweekled(hours);
    }

    @Override
    public FederalCurriculumRequirement reparted(final PartType part) {
        return this.origin.reparted(part);
    }
}
