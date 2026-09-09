/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement.filter;

import com.eshabakhov.schoodule.filter.FlConditional;
import org.jooq.Condition;

/**
 * Federal curriculum requirement grade filter.
 *
 * @since 0.0.1
 */
public final class FlCdFcrByGrade implements FlConditional {

    /**
     * JOOQ Table for FederalCurriculumRequirement.
     */
    private static final com.eshabakhov.schoodule.tables.FederalCurriculumRequirement REQUIREMENT =
        com.eshabakhov.schoodule.tables.FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT;

    /**
     * Origin filter.
     */
    private final FlConditional origin;

    /**
     * Grade to search.
     */
    private final Integer grade;

    public FlCdFcrByGrade(final FlConditional origin, final Integer grade) {
        this.origin = origin;
        this.grade = grade;
    }

    @Override
    public Condition condition() {
        Condition condition = this.origin.condition();
        if (this.grade != null) {
            condition = condition.and(FlCdFcrByGrade.REQUIREMENT.GRADE.eq(this.grade));
        }
        return condition;
    }
}
