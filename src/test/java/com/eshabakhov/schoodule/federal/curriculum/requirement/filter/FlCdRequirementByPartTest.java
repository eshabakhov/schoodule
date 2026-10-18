/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement.filter;

import com.eshabakhov.schoodule.enums.CurriculumPartType;
import com.eshabakhov.schoodule.filter.FlCdTrue;
import com.eshabakhov.schoodule.tables.FederalCurriculumRequirement;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test for {@link FlCdRequirementByPart}.
 *
 * @since 0.0.1
 */
final class FlCdRequirementByPartTest {

    @ParameterizedTest
    @ValueSource(strings = {"MANDATORY", "OPTIONAL"})
    void addsPartToCondition(final String part) {
        MatcherAssert.assertThat(
            "Part condition must include the part",
            new FlCdRequirementByPart(
                new FlCdTrue(),
                part
            ).condition(),
            Matchers.is(
                DSL.trueCondition().and(
                    FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                        .PART_TYPE.eq(CurriculumPartType.valueOf(part))
                )
            )
        );
    }

    @Test
    void keepsAcceptingConditionForNullPart() {
        MatcherAssert.assertThat(
            "Null part must keep the accepting condition",
            new FlCdRequirementByPart(
                new FlCdTrue(),
                null
            ).condition(),
            Matchers.is(DSL.trueCondition())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "\t"})
    void keepsAcceptingConditionForEmptyPart(final String part) {
        MatcherAssert.assertThat(
            "Empty part must keep the accepting condition",
            new FlCdRequirementByPart(
                new FlCdTrue(),
                part
            ).condition(),
            Matchers.is(DSL.trueCondition())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"mandatory", "UNKNOWN"})
    void rejectsUnknownPart(final String part) {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new FlCdRequirementByPart(
                new FlCdTrue(),
                part
            )
        );
    }
}
