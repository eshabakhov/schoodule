/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.curriculum;

import com.eshabakhov.schoodule.Media;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Base in-memory implementation of {@link FederalCurriculumRequirement}.
 * Holds only rid, grade, and part as the minimal set.
 * Subject and hours are added via decorators.
 *
 * @since 0.0.1
 */
@ToString(of = {"rid", "grade"})
@EqualsAndHashCode(of = "rid")
public final class FcrBase implements FederalCurriculumRequirement {

    /**
     * Federal curriculum requirement id.
     */
    private final long rid;

    /**
     * Grade.
     */
    private final Integer grade;

    /**
     * Curriculum part type.
     */
    private final PartType part;

    /**
     * Creates a base requirement.
     *
     * @param rid      Unique identifier
     * @param grade    School grade (1–11)
     * @param part Curriculum part type
     */
    public FcrBase(final long rid, final Integer grade, final PartType part) {
        this.rid = rid;
        this.grade = grade;
        this.part = part;
    }

    @Override
    public Long uid() {
        return this.rid;
    }

    @Override
    public Media print(final Media media) {
        return media.with("id", this.rid)
            .with("grade", this.grade)
            .with("part", this.part.name());
    }

    @Override
    public FederalCurriculumRequirement regraded(final Integer grd) {
        return new FcrBase(this.rid, grd, this.part);
    }

    @Override
    public FederalCurriculumRequirement resubjected(final String subject) {
        return new FcrSubjected(this, subject);
    }

    @Override
    public FederalCurriculumRequirement reweekled(final Integer hours) {
        return new FcrWeekled(this, hours);
    }

    @Override
    public FederalCurriculumRequirement reparted(final PartType prt) {
        return new FcrBase(this.rid, this.grade, prt);
    }
}
