/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.federal.curriculum.requirement.filter;

import com.eshabakhov.surok.filter.FlConditional;
import com.eshabakhov.surok.sql.ClasspathSql;
import java.util.Optional;
import org.jooq.Condition;
import org.jooq.impl.DSL;

/**
 * Federal curriculum requirement grade filter.
 *
 * @since 0.0.1
 */
public final class FlCdRequirementByGrade implements FlConditional {

    /**
     * Origin filter.
     */
    private final FlConditional origin;

    /**
     * Grade to search.
     */
    private final Optional<Integer> grade;

    /**
     * New grade filter from a string value.
     *
     * @param origin Origin filter
     * @param grade Grade to search
     */
    public FlCdRequirementByGrade(final FlConditional origin, final String grade) {
        this(
            origin,
            Optional.ofNullable(grade).filter(item -> !item.isBlank()).map(Integer::valueOf)
        );
    }

    private FlCdRequirementByGrade(final FlConditional origin, final Optional<Integer> grade) {
        this.origin = origin;
        this.grade = grade;
    }

    @Override
    public Condition condition() {
        Condition condition = this.origin.condition();
        if (this.grade.isPresent()) {
            condition = condition.and(
                DSL.condition(
                    new ClasspathSql("curriculum/requirement/conditions/by-grade.sql").text(),
                    this.grade.get()
                )
            );
        }
        return condition;
    }
}
