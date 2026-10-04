/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement.filter;

import com.eshabakhov.schoodule.filter.FlConditional;
import com.eshabakhov.schoodule.tables.FederalCurriculumRequirement;
import java.util.Optional;
import org.jooq.Condition;

/**
 * Federal curriculum requirement grade filter.
 *
 * @since 0.0.1
 */
public final class FlCdFcrByGrade implements FlConditional {

    /**
     * Origin filter.
     */
    private final FlConditional origin;

    /**
     * Grade to search.
     */
    private final Optional<Integer> grade;

    /**
     * New grade filter.
     *
     * @param origin Origin filter
     * @param grade Grade to search
     */
    public FlCdFcrByGrade(final FlConditional origin, final Integer grade) {
        this(origin, Optional.ofNullable(grade));
    }

    /**
     * New grade filter from a string value.
     *
     * @param origin Origin filter
     * @param grade Grade to search
     */
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
            condition = condition.and(
                FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT.GRADE.eq(
                    this.grade.get()
                )
            );
        }
        return condition;
    }
}
