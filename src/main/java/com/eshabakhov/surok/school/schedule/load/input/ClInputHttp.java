/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.schedule.load.input;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.school.schedule.load.ClassLoadInput;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.util.MultiValueMap;

/**
 * A load input read from canonical form fields or a versioned JSON document.
 *
 * @since 0.0.1
 */
public final class ClInputHttp implements ClassLoadInput {

    /** Composed input. */
    private final ClassLoadInput origin;

    /**
     * New form input.
     *
     * @param fields Submitted fields
     */
    public ClInputHttp(final MultiValueMap<String, String> fields) {
        this(
            new ClInputNumber(
                new ClInputNumber(
                    new ClInputNumber(
                        new ClInput(), "schoolClassId", fields.getFirst("schoolClassId")
                    ),
                    "subjectId", fields.getFirst("subjectId")
                ), "hoursPerWeek", fields.getFirst("hoursPerWeek")
            )
        );
    }

    /**
     * New JSON input.
     *
     * @param json Submitted document
     */
    public ClInputHttp(final JsonNode json) {
        this(
            new ClInputNumber(
                new ClInputNumber(
                    new ClInputNumber(
                        new ClInput(), "schoolClassId", json.path("schoolClassId").asText(null)
                    ), "subjectId", json.path("subjectId").asText(null)
                ), "hoursPerWeek", json.path("hoursPerWeek").asText(null)
            )
        );
    }

    private ClInputHttp(final ClassLoadInput origin) {
        this.origin = origin;
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return this.origin.print(media);
    }
}
