/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link RiFake}.
 *
 * @since 0.0.1
 */
final class RiFakeTest {

    @Test
    void exposesSchool() {
        MatcherAssert.assertThat(
            "Fake reference information must expose its school",
            new RiFake(7L, true, false).school(),
            Matchers.is(7L)
        );
    }

    @Test
    void exposesCorporateFlag() {
        MatcherAssert.assertThat(
            "Fake reference information must expose its corporate flag",
            new RiFake(7L, true, false).corporate(),
            Matchers.is(true)
        );
    }

    @Test
    void exposesAliveFlag() {
        MatcherAssert.assertThat(
            "Fake reference information must expose its alive flag",
            new RiFake(7L, true, false).alive(),
            Matchers.is(false)
        );
    }
}
