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
 * Test for {@link RqInputSubject}.
 *
 * @since 0.0.1
 */
final class RqInputSubjectTest {

    @ParameterizedTest
    @ValueSource(strings = {" Mathematics ", "A"})
    void printsTrimmedSubject(final String subject) throws Exception {
        MatcherAssert.assertThat(
            "Subject must be printed without surrounding spaces",
            new RqInputSubject(new RqInput(), subject)
                .print(new MdRqInput()).print(),
            Matchers.is(Map.of("subjectName", subject.trim()))
        );
    }

    @Test
    void rejectsNullSubject() {
        Assertions.assertThrows(
            RequirementInputException.class,
            () -> new RqInputSubject(new RqInput(), null)
                .print(new MdRqInput())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t"})
    void rejectsBlankSubject(final String subject) {
        Assertions.assertThrows(
            RequirementInputException.class,
            () -> new RqInputSubject(new RqInput(), subject)
                .print(new MdRqInput())
        );
    }
}
