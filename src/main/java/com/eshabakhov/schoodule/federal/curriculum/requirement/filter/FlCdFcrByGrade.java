/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement.filter;

import com.eshabakhov.schoodule.filter.FlConditional;
import java.util.Optional;
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
    private final Optional<Integer> grade;

    public FlCdFcrByGrade(final FlConditional origin, final Integer grade) {
        this(origin, Optional.ofNullable(grade));
    }

    public FlCdFcrByGrade(final FlConditional origin, final String grade) {
        this(
            origin,
            Optional.ofNullable(grade).filter(item -> !item.isBlank()).map(Integer::valueOf)
        );
    }

    private FlCdFcrByGrade(final FlConditional origin, final Optional<Integer> grade) {
        this.origin = origin;
        this.grade = grade;
    }

    @Override
    public Condition condition() {
        Condition condition = this.origin.condition();
        if (this.grade.isPresent()) {
            condition = condition.and(FlCdFcrByGrade.REQUIREMENT.GRADE.eq(this.grade.get()));
        }
        return condition;
    }
}
