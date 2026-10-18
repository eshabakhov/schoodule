/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement.filter;

import com.eshabakhov.schoodule.filter.FlCdTrue;
import com.eshabakhov.schoodule.tables.FederalCurriculumRequirement;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test for {@link FlCdRequirementBySubject}.
 *
 * @since 0.0.1
 */
final class FlCdRequirementBySubjectTest {

    @Test
    void addsTrimmedSubjectToCondition() {
        MatcherAssert.assertThat(
            "Subject condition must include the trimmed subject",
            new FlCdRequirementBySubject(
                new FlCdTrue(),
                "  Mathematics  "
            ).condition(),
            Matchers.is(
                DSL.trueCondition().and(
                    FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                        .SUBJECT_NAME.likeIgnoreCase("%Mathematics%")
                )
            )
        );
    }

    @Test
    void acceptsOneCharacterSubject() {
        MatcherAssert.assertThat(
            "Subject condition must include a one-character subject",
            new FlCdRequirementBySubject(
                new FlCdTrue(),
                "Я"
            ).condition(),
            Matchers.is(
                DSL.trueCondition().and(
                    FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                        .SUBJECT_NAME.likeIgnoreCase("%Я%")
                )
            )
        );
    }

    @Test
    void keepsAcceptingConditionForNullSubject() {
        MatcherAssert.assertThat(
            "Null subject must keep the accepting condition",
            new FlCdRequirementBySubject(
                new FlCdTrue(),
                null
            ).condition(),
            Matchers.is(DSL.trueCondition())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "\t"})
    void keepsAcceptingConditionForEmptySubject(final String subject) {
        MatcherAssert.assertThat(
            "Empty subject must keep the accepting condition",
            new FlCdRequirementBySubject(
                new FlCdTrue(),
                subject
            ).condition(),
            Matchers.is(DSL.trueCondition())
        );
    }
}
