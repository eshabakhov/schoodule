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
    void createsExceptionWithoutCause() {
        MatcherAssert.assertThat(
            "Single-argument exception must retain its state",
            new RequirementInputException("Invalid input"),
            Matchers.allOf(
                Matchers.hasProperty(
                    "message", Matchers.is("Invalid input")
                ),
                Matchers.hasProperty("cause", Matchers.nullValue())
            )
        );
    }

    @Test
    void createsExceptionWithCause() {
        final Throwable cause = new IllegalArgumentException("Invalid value");
        MatcherAssert.assertThat(
            "Two-argument exception must retain its state",
            new RequirementInputException("Invalid input", cause),
            Matchers.allOf(
                Matchers.hasProperty(
                    "message", Matchers.is("Invalid input")
                ),
                Matchers.hasProperty(
                    "cause", Matchers.sameInstance(cause)
                )
            )
        );
    }
}
