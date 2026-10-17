/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule;

import com.eshabakhov.schoodule.federal.curriculum.requirement.input.RequirementInputException;
import com.eshabakhov.schoodule.federal.curriculum.requirement.input.RqInputHttp;
import com.eshabakhov.schoodule.media.MdJson;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Tests federal curriculum requirement input.
 *
 * @since 0.1.0
 */
final class RequirementInputTest {

    @Test
    void printsDecoratedHttpInput() throws Exception {
        final MultiValueMap<String, String> parameters =
            new LinkedMultiValueMap<>();
        parameters.add("grade", " 5 ");
        parameters.add("subjectName", " Mathematics ");
        parameters.add("weeklyHours", " 4 ");
        parameters.add("partType", "MANDATORY");
        Assertions.assertEquals(
            JsonNodeFactory.instance.objectNode()
                .put("grade", 5)
                .put("subjectName", "Mathematics")
                .put("weeklyHours", 4)
                .put("partType", "MANDATORY"),
            new RqInputHttp(parameters).print(new MdJson()).print()
        );
    }

    @Test
    void rejectsMissingRequiredInput() {
        Assertions.assertThrows(
            RequirementInputException.class,
            () -> new RqInputHttp(
                new LinkedMultiValueMap<>(0)
            ).print(new MdJson())
        );
    }
}
