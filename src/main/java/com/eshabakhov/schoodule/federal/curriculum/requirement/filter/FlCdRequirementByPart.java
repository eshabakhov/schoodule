/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement.filter;

import com.eshabakhov.schoodule.enums.CurriculumPartType;
import com.eshabakhov.schoodule.federal.curriculum.requirement.Requirement;
import com.eshabakhov.schoodule.filter.FlConditional;
import com.eshabakhov.schoodule.tables.FederalCurriculumRequirement;
import java.util.Optional;
import org.jooq.Condition;

/**
 * Federal curriculum requirement part filter.
 *
 * @since 0.0.1
 */
public final class FlCdRequirementByPart implements FlConditional {

    /**
     * Origin filter.
     */
    private final FlConditional origin;

    /**
     * Part to search.
     */
    private final Optional<Requirement.PartType> part;

    /**
     * New part filter.
     *
     * @param origin Origin filter
     * @param part Part to search
     */
    public FlCdRequirementByPart(
        final FlConditional origin,
        final Requirement.PartType part
    ) {
        this(origin, Optional.ofNullable(part));
    }

    /**
     * New part filter from a string value.
     *
     * @param origin Origin filter
     * @param part Part to search
     */
    public FlCdRequirementByPart(final FlConditional origin, final String part) {
        this(
            origin,
            Optional.ofNullable(part)
                .filter(item -> !item.isBlank())
                .map(Requirement.PartType::valueOf)
        );
    }

    private FlCdRequirementByPart(
        final FlConditional origin,
        final Optional<Requirement.PartType> part
    ) {
        this.origin = origin;
        this.part = part;
    }

    @Override
    public Condition condition() {
        Condition condition = this.origin.condition();
        if (this.part.isPresent()) {
            condition = condition.and(
                FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT.PART_TYPE.eq(
                    CurriculumPartType.valueOf(this.part.get().name())
                )
            );
        }
        return condition;
    }
}
