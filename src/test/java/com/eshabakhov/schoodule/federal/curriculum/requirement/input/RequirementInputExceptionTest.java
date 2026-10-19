/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement.input;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link RequirementInputException}.
 *
 * @since 0.0.1
 */
final class RequirementInputExceptionTest {

    @Test
    void retainsMessageWithoutCause() {
        MatcherAssert.assertThat(
            "Single-argument exception must retain its message",
            new RequirementInputException("Invalid input").getMessage(),
            Matchers.is("Invalid input")
        );
    }

    @Test
    void exposesEmptyCause() {
        MatcherAssert.assertThat(
            "Single-argument exception must expose an empty cause",
            new RequirementInputException("Invalid input").getCause(),
            Matchers.nullValue()
        );
    }

    @Test
    void retainsMessageWithCause() {
        MatcherAssert.assertThat(
            "Two-argument exception must retain its message",
            new RequirementInputException(
                "Invalid input", new IllegalArgumentException("Invalid value")
            ).getMessage(),
            Matchers.is("Invalid input")
        );
    }

    @Test
    void retainsCause() {
        final Throwable cause = new IllegalArgumentException("Invalid value");
        MatcherAssert.assertThat(
            "Two-argument exception must retain its cause",
            new RequirementInputException("Invalid input", cause).getCause(),
            Matchers.sameInstance(cause)
        );
    }
}
