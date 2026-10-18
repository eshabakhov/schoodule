/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement.input;

import java.util.Map;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test for {@link RqInputGrade}.
 *
 * @since 0.0.1
 */
final class RqInputGradeTest {

    @ParameterizedTest
    @ValueSource(strings = {" -2147483648 ", "0", " 2147483647 "})
    void printsIntegerGrade(final String grade) throws Exception {
        MatcherAssert.assertThat(
            "Grade must be parsed and printed without surrounding spaces",
            new RqInputGrade(new RqInput(), grade)
                .print(new MdRqInput()).print(),
            Matchers.is(Map.of("grade", grade.trim()))
        );
    }

    @Test
    void rejectsNullGrade() {
        Assertions.assertThrows(
            RequirementInputException.class,
            () -> new RqInputGrade(new RqInput(), null)
                .print(new MdRqInput())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t"})
    void rejectsBlankGrade(final String grade) {
        Assertions.assertThrows(
            RequirementInputException.class,
            () -> new RqInputGrade(new RqInput(), grade)
                .print(new MdRqInput())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "2147483648", "-2147483649"})
    void rejectsInvalidGrade(final String grade) {
        Assertions.assertThrows(
            RequirementInputException.class,
            () -> new RqInputGrade(new RqInput(), grade)
                .print(new MdRqInput())
        );
    }
}
