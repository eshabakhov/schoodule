/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.building.input;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.school.building.BuildingInput;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.util.MultiValueMap;

/**
 * HTTP input for building, for example {@code new BdInputHttp(parameters)}.
 *
 * @since 0.0.1
 */
public final class BdInputHttp implements BuildingInput {

    /** Composed input. */
    private final BuildingInput origin;

    /**
     * New form input.
     *
     * @param parameters Form fields
     */
    public BdInputHttp(final MultiValueMap<String, String> parameters) {
        this(new BdInputName(new BdInput(), parameters.getFirst("name")));
    }

    /**
     * New JSON input.
     *
     * @param json Request document
     */
    public BdInputHttp(final JsonNode json) {
        this(new BdInputName(new BdInput(), json.path("name").asText(null)));
    }

    /**
     * New composed HTTP input.
     *
     * @param origin Validated input
     */
    private BdInputHttp(final BuildingInput origin) {
        this.origin = origin;
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return this.origin.print(media);
    }
}
