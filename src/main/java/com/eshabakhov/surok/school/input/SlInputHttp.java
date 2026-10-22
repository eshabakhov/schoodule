/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.input;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.school.SchoolInput;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.util.MultiValueMap;

/**
 * HTTP input for school, for example {@code new SlInputHttp(parameters)}.
 *
 * @since 0.0.1
 */
public final class SlInputHttp implements SchoolInput {

    /** Composed input. */
    private final SchoolInput origin;

    /**
     * New form input.
     *
     * @param parameters Form fields
     */
    public SlInputHttp(final MultiValueMap<String, String> parameters) {
        this(new SlInputName(new SlInput(), parameters.getFirst("name")));
    }

    /**
     * New JSON input.
     *
     * @param json Request document
     */
    public SlInputHttp(final JsonNode json) {
        this(new SlInputName(new SlInput(), json.path("name").asText(null)));
    }

    /**
     * New composed HTTP input.
     *
     * @param origin Validated input
     */
    private SlInputHttp(final SchoolInput origin) {
        this.origin = origin;
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return this.origin.print(media);
    }
}
