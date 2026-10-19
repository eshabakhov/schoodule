/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.user;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link CdFake}.
 *
 * @since 0.0.1
 */
final class CdFakeTest {

    @Test
    void exposesUsername() {
        MatcherAssert.assertThat(
            "Fake credentials must expose their username",
            new CdFake("admin", "secret").username(),
            Matchers.is("admin")
        );
    }

    @Test
    void exposesPassword() {
        MatcherAssert.assertThat(
            "Fake credentials must expose their password",
            new CdFake("admin", "secret").password(),
            Matchers.is("secret")
        );
    }
}
