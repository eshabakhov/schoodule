/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement.input;

import java.util.Map;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Test for {@link RqInputHttp}.
 *
 * @since 0.0.1
 */
final class RqInputHttpTest {

    @Test
    void printsFirstHttpValues() throws Exception {
        final MultiValueMap<String, String> parameters =
            new LinkedMultiValueMap<>();
        parameters.add("grade", " 5 ");
        parameters.add("grade", "6");
        parameters.add("subjectName", " Mathematics ");
        parameters.add("weeklyHours", " 4 ");
        parameters.add("partType", " MANDATORY ");
        MatcherAssert.assertThat(
            "HTTP input must compose all requirement fields",
            new RqInputHttp(parameters).print(new MdRqInput()).print(),
            Matchers.is(
                Map.of(
                    "grade", "5",
                    "subjectName", "Mathematics",
                    "weeklyHours", "4",
                    "partType", "MANDATORY"
                )
            )
        );
    }

    @Test
    void rejectsMissingHttpValues() {
        Assertions.assertThrows(
            RequirementInputException.class,
            () -> new RqInputHttp(new LinkedMultiValueMap<>(0))
                .print(new MdRqInput())
        );
    }

    @Test
    void rejectsNullParameters() {
        Assertions.assertThrows(
            NullPointerException.class,
            () -> new RqInputHttp(null).print(new MdRqInput())
        );
    }
}
