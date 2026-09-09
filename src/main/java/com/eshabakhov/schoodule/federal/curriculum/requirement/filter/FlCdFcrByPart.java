/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement.filter;

import com.eshabakhov.schoodule.enums.CurriculumPartType;
import com.eshabakhov.schoodule.federal.curriculum.FederalCurriculumRequirement;
import com.eshabakhov.schoodule.filter.FlConditional;
import org.jooq.Condition;

/**
 * Federal curriculum requirement part filter.
 *
 * @since 0.0.1
 */
public final class FlCdFcrByPart implements FlConditional {

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
     * Part to search.
     */
    private final FederalCurriculumRequirement.PartType part;

    public FlCdFcrByPart(
        final FlConditional origin,
        final FederalCurriculumRequirement.PartType part
    ) {
        this.origin = origin;
        this.part = part;
    }

    @Override
    public Condition condition() {
        Condition condition = this.origin.condition();
        if (this.part != null) {
            condition = condition.and(
                FlCdFcrByPart.REQUIREMENT.PART_TYPE.eq(CurriculumPartType.valueOf(this.part.name()))
            );
        }
        return condition;
    }
}
