/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.federal.curriculum.requirement.input;

import java.util.Map;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test for {@link RqInputPart}.
 *
 * @since 0.0.1
 */
final class RqInputPartTest {

    @ParameterizedTest
    @ValueSource(strings = {" MANDATORY ", "OPTIONAL"})
    void printsSupportedPart(final String part) throws Exception {
        MatcherAssert.assertThat(
            "Supported part must be printed without surrounding spaces",
            new RqInputPart(new RqInput(), part)
                .print(new MdRqInput()).print(),
            Matchers.is(Map.of("partType", part.trim()))
        );
    }

    @Test
    void rejectsNullPart() {
        Assertions.assertThrows(
            RequirementInputException.class,
            () -> new RqInputPart(new RqInput(), null)
                .print(new MdRqInput())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t"})
    void rejectsBlankPart(final String part) {
        Assertions.assertThrows(
            RequirementInputException.class,
            () -> new RqInputPart(new RqInput(), part)
                .print(new MdRqInput())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"mandatory", "CORE"})
    void rejectsUnsupportedPart(final String part) {
        Assertions.assertThrows(
            RequirementInputException.class,
            () -> new RqInputPart(new RqInput(), part)
                .print(new MdRqInput())
        );
    }
}
