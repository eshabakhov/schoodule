/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.schoolclass.input;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.school.schoolclass.SchoolClassInput;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.util.MultiValueMap;

/**
 * HTTP input for school class, for example {@code new ScInputHttp(parameters)}.
 *
 * @since 0.0.1
 */
public final class ScInputHttp implements SchoolClassInput {

    /** Composed input. */
    private final SchoolClassInput origin;

    /**
     * New form input.
     *
     * @param parameters Form fields
     */
    public ScInputHttp(final MultiValueMap<String, String> parameters) {
        this(new ScInputGrade(
            new ScInputLitera(new ScInput(), parameters.getFirst("litera")),
            parameters.getFirst("grade")
        ));
    }

    /**
     * New JSON input.
     *
     * @param json Request document
     */
    public ScInputHttp(final JsonNode json) {
        this(new ScInputGrade(
            new ScInputLitera(new ScInput(), json.path("litera").asText(null)),
            json.path("grade").asText(null)
        ));
    }

    /**
     * New composed HTTP input.
     *
     * @param origin Validated input
     */
    private ScInputHttp(final SchoolClassInput origin) {
        this.origin = origin;
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return this.origin.print(media);
    }
}
