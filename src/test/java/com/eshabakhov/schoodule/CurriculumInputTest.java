/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule;

import com.eshabakhov.schoodule.federal.curriculum.input.CrInputHttp;
import com.eshabakhov.schoodule.federal.curriculum.input.CurriculumInputException;
import com.eshabakhov.schoodule.media.MdJson;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Tests federal curriculum input.
 *
 * @since 0.1.0
 */
final class CurriculumInputTest {

    @Test
    void printsDecoratedHttpInput() throws Exception {
        final MultiValueMap<String, String> parameters =
            new LinkedMultiValueMap<>();
        parameters.add("title", " Plan ");
        parameters.add("level", "BASIC");
        parameters.add("week", "FIVE_DAYS");
        parameters.add("version", " 2 ");
        parameters.add("year", " 2026/2027 ");
        parameters.add("description", " Description ");
        Assertions.assertEquals(
            JsonNodeFactory.instance.objectNode()
                .put("title", "Plan")
                .put("level", "BASIC")
                .put("week", "FIVE_DAYS")
                .put("version", "2")
                .put("year", "2026/2027")
                .put("description", "Description"),
            new CrInputHttp(parameters).print(new MdJson()).print()
        );
    }

    @Test
    void rejectsMissingRequiredInput() {
        Assertions.assertThrows(
            CurriculumInputException.class,
            () -> new CrInputHttp(
                new LinkedMultiValueMap<>(0)
            ).print(new MdJson())
        );
    }
}
