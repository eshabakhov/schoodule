/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Tests for federal curriculum requirement exceptions.
 *
 * @since 0.0.1
 */
final class RequirementExceptionsTest {

    @Test
    void describesExistingRequirement() {
        MatcherAssert.assertThat(
            "Existing requirement exception must describe the conflict",
            new RequirementAlreadyExistsException().getMessage(),
            Matchers.is("Federal curriculum requirement already exists")
        );
    }

    @Test
    void describesFailedCreation() {
        MatcherAssert.assertThat(
            "Creation exception must describe the failure",
            new RequirementFailedCreateException().getMessage(),
            Matchers.is("Failed to create federal curriculum requirement")
        );
    }

    @Test
    void keepsNotFoundMessage() {
        MatcherAssert.assertThat(
            "Not-found exception must preserve its message",
            new RequirementNotFoundException("Requirement 7 was not found")
                .getMessage(),
            Matchers.is("Requirement 7 was not found")
        );
    }

    @Test
    void acceptsNullNotFoundMessage() {
        MatcherAssert.assertThat(
            "Not-found exception must accept a null message",
            new RequirementNotFoundException(null).getMessage(),
            Matchers.nullValue()
        );
    }
}
