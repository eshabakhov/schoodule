/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.federal.curriculum.requirement;

import com.eshabakhov.surok.federal.curriculum.requirement.input.RqInput;
import com.eshabakhov.surok.federal.curriculum.requirement.input.RqInputGrade;
import com.eshabakhov.surok.federal.curriculum.requirement.input.RqInputHours;
import com.eshabakhov.surok.federal.curriculum.requirement.input.RqInputPart;
import com.eshabakhov.surok.federal.curriculum.requirement.input.RqInputSubject;
import com.eshabakhov.surok.media.MdJson;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link RqFake}.
 *
 * @since 0.0.1
 */
final class RqFakeTest {

    @Test
    void printsRequirement() throws Exception {
        MatcherAssert.assertThat(
            "Fake requirement must print its identity and input",
            new RqFake(
                17L,
                new RqInputPart(
                    new RqInputHours(
                        new RqInputSubject(
                            new RqInputGrade(new RqInput(), "5"),
                            "Mathematics"
                        ),
                        "6"
                    ),
                    "MANDATORY"
                )
            ).print(new MdJson()).print(),
            Matchers.is(
                JsonNodeFactory.instance.objectNode()
                    .put("id", 17L)
                    .put("grade", 5)
                    .put("subjectName", "Mathematics")
                    .put("weeklyHours", 6)
                    .put("partType", "MANDATORY")
                    .put("internal", "fake")
            )
        );
    }

    @Test
    void revisesRequirement() throws Exception {
        MatcherAssert.assertThat(
            "Fake requirement revision must preserve its identity",
            new RqFake(21L, new RqInput()).revise(
                new RqInputGrade(new RqInput(), "9")
            ).print(new MdJson()).print(),
            Matchers.is(
                JsonNodeFactory.instance.objectNode()
                    .put("id", 21L)
                    .put("grade", 9)
                    .put("internal", "fake")
            )
        );
    }
}
