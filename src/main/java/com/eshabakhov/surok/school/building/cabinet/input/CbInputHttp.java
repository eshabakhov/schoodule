/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.building.cabinet.input;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.school.building.cabinet.CabinetInput;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.util.MultiValueMap;

/**
 * HTTP input for cabinet, for example {@code new CbInputHttp(parameters)}.
 *
 * @since 0.0.1
 */
public final class CbInputHttp implements CabinetInput {

    /** Composed input. */
    private final CabinetInput origin;

    /**
     * New form input.
     *
     * @param parameters Form fields
     */
    public CbInputHttp(final MultiValueMap<String, String> parameters) {
        this(new CbInputName(new CbInput(), parameters.getFirst("name")));
    }

    /**
     * New JSON input.
     *
     * @param json Request document
     */
    public CbInputHttp(final JsonNode json) {
        this(new CbInputName(new CbInput(), json.path("name").asText(null)));
    }

    /**
     * New composed HTTP input.
     *
     * @param origin Validated input
     */
    private CbInputHttp(final CabinetInput origin) {
        this.origin = origin;
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return this.origin.print(media);
    }
}
