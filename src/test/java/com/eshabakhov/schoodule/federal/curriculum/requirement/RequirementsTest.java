/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement;

import com.eshabakhov.schoodule.federal.curriculum.requirement.input.RqInput;
import com.eshabakhov.schoodule.federal.curriculum.requirement.input.RqInputGrade;
import com.eshabakhov.schoodule.media.MdJson;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link Requirements}.
 *
 * @since 0.0.1
 */
final class RequirementsTest {

    @Test
    void revisesSelectedRequirement() throws Exception {
        MatcherAssert.assertThat(
            "Revision must be delegated to the selected requirement",
            new RqsFake(
                new LinkedHashMap<>(
                    Map.of(7L, new RqFake(7L, new RqInput()))
                ),
                new AtomicLong(7L)
            ).revise(
                7L, new RqInputGrade(new RqInput(), "7")
            ).print(new MdJson()).print(),
            Matchers.is(
                JsonNodeFactory.instance.objectNode()
                    .put("id", 7L)
                    .put("grade", 7)
                    .put("internal", "fake")
            )
        );
    }
}
