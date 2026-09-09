/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement.filter;

import com.eshabakhov.schoodule.filter.FlConditional;
import org.jooq.Condition;

/**
 * Federal curriculum requirement subject filter.
 *
 * @since 0.0.1
 */
public final class FlCdFcrBySubject implements FlConditional {

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
     * Subject to search.
     */
    private final String subject;

    public FlCdFcrBySubject(final FlConditional origin, final String subject) {
        this.origin = origin;
        this.subject = subject;
    }

    @Override
    public Condition condition() {
        Condition condition = this.origin.condition();
        if (this.subject != null && !this.subject.isBlank()) {
            condition = condition.and(
                FlCdFcrBySubject.REQUIREMENT.SUBJECT_NAME.likeIgnoreCase(
                    String.format("%%%s%%", this.subject.trim())
                )
            );
        }
        return condition;
    }
}
