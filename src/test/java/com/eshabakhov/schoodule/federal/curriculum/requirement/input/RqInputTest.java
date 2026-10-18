/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement.input;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link RqInput}.
 *
 * @since 0.0.1
 */
final class RqInputTest {

    @Test
    void keepsMediaUnchanged() {
        final MdRqInput media = new MdRqInput().with("existing", "value");
        MatcherAssert.assertThat(
            "Empty input must return the supplied media",
            new RqInput().print(media),
            Matchers.sameInstance(media)
        );
    }
}
