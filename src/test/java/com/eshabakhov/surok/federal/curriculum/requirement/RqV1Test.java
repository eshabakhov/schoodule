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
 * Test for {@link RqV1}.
 *
 * @since 0.0.1
 */
final class RqV1Test {

    @Test
    void delegatesIdentity() {
        MatcherAssert.assertThat(
            "Versioned requirement must preserve its identity",
            new RqV1(new RqFake(13L, new RqInput())).uid(),
            Matchers.is(13L)
        );
    }

    @Test
    void printsOnlyVersionOneFields() throws Exception {
        MatcherAssert.assertThat(
            "Version one must expose only its public fields",
            new RqV1(
                new RqFake(
                    7L,
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
                )
            ).print(new MdJson()).print(),
            Matchers.is(
                JsonNodeFactory.instance.objectNode()
                    .put("id", 7L)
                    .put("grade", 5)
                    .put("subjectName", "Mathematics")
                    .put("weeklyHours", 6)
                    .put("partType", "MANDATORY")
            )
        );
    }

    @Test
    void delegatesRevision() throws Exception {
        MatcherAssert.assertThat(
            "Versioned requirement must delegate revision",
            new RqV1(new RqFake(11L, new RqInput())).revise(
                new RqInputGrade(new RqInput(), "8")
            ).print(new MdJson()).print(),
            Matchers.is(
                JsonNodeFactory.instance.objectNode()
                    .put("id", 11L)
                    .put("grade", 8)
                    .put("internal", "fake")
            )
        );
    }
}
