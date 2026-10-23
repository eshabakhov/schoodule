/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.schedule.input;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.school.schedule.ScheduleInput;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.util.MultiValueMap;

/**
 * HTTP input for schedule, for example {@code new SdInputHttp(parameters)}.
 *
 * @since 0.0.1
 */
public final class SdInputHttp implements ScheduleInput {

    /** Composed input. */
    private final ScheduleInput origin;

    /**
     * New form input.
     *
     * @param parameters Form fields
     */
    public SdInputHttp(final MultiValueMap<String, String> parameters) {
        this(new SdInputName(new SdInput(), parameters.getFirst("name")));
    }

    /**
     * New JSON input.
     *
     * @param json Request document
     */
    public SdInputHttp(final JsonNode json) {
        this(new SdInputName(new SdInput(), json.path("name").asText(null)));
    }

    /**
     * New composed HTTP input.
     *
     * @param origin Validated input
     */
    private SdInputHttp(final ScheduleInput origin) {
        this.origin = origin;
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return this.origin.print(media);
    }
}
