/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.federal.curriculum.requirement.filter;

import com.eshabakhov.surok.filter.FlCdTrue;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test for {@link FlCdRequirementByGrade}.
 *
 * @since 0.0.1
 */
final class FlCdRequirementByGradeTest {

    @Test
    void addsGradeToCondition() {
        MatcherAssert.assertThat(
            "Grade condition must include the grade",
            new FlCdRequirementByGrade(
                new FlCdTrue(),
                "7"
            ).condition(),
            Matchers.is(
                DSL.trueCondition().and(
                    DSL.condition(
                        """
                            federal_curriculum_requirement.grade = ?
                            """,
                        7
                    )
                )
            )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "11"})
    void acceptsBoundaryGrades(final String grade) {
        MatcherAssert.assertThat(
            "Grade condition must include a boundary grade",
            new FlCdRequirementByGrade(
                new FlCdTrue(),
                grade
            ).condition(),
            Matchers.is(
                DSL.trueCondition().and(
                    DSL.condition(
                        """
                            federal_curriculum_requirement.grade = ?
                            """,
                        Integer.valueOf(grade)
                    )
                )
            )
        );
    }

    @Test
    void keepsAcceptingConditionForNullGrade() {
        MatcherAssert.assertThat(
            "Null grade must keep the accepting condition",
            new FlCdRequirementByGrade(
                new FlCdTrue(),
                null
            ).condition(),
            Matchers.is(DSL.trueCondition())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "\t"})
    void keepsAcceptingConditionForEmptyGrade(final String grade) {
        MatcherAssert.assertThat(
            "Empty grade must keep the accepting condition",
            new FlCdRequirementByGrade(
                new FlCdTrue(),
                grade
            ).condition(),
            Matchers.is(DSL.trueCondition())
        );
    }

    @Test
    void rejectsNonNumericGrade() {
        Assertions.assertThrows(
            NumberFormatException.class,
            () -> new FlCdRequirementByGrade(
                new FlCdTrue(),
                "seventh"
            )
        );
    }

    @Test
    void rejectsGradeOutsideIntegerRange() {
        Assertions.assertThrows(
            NumberFormatException.class,
            () -> new FlCdRequirementByGrade(
                new FlCdTrue(),
                "2147483648"
            )
        );
    }
}
