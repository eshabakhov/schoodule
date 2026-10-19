/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.user;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link RlFake}.
 *
 * @since 0.0.1
 */
final class RlFakeTest {

    @Test
    void exposesIdentity() {
        MatcherAssert.assertThat(
            "Fake role must expose its identity",
            new RlFake(4L, "ADMIN").uid(),
            Matchers.is(4L)
        );
    }

    @Test
    void exposesName() {
        MatcherAssert.assertThat(
            "Fake role must expose its name",
            new RlFake(4L, "ADMIN").name(),
            Matchers.is("ADMIN")
        );
    }
}
