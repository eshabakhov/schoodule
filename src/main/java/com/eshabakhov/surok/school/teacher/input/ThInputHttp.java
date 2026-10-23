/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.teacher.input;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.school.teacher.TeacherInput;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.util.MultiValueMap;

/**
 * HTTP input for teacher, for example {@code new ThInputHttp(parameters)}.
 *
 * @since 0.0.1
 */
public final class ThInputHttp implements TeacherInput {

    /** Composed input. */
    private final TeacherInput origin;

    /**
     * New form input.
     *
     * @param parameters Form fields
     */
    public ThInputHttp(final MultiValueMap<String, String> parameters) {
        this(new ThInputName(new ThInput(), parameters.getFirst("name")));
    }

    /**
     * New JSON input.
     *
     * @param json Request document
     */
    public ThInputHttp(final JsonNode json) {
        this(new ThInputName(new ThInput(), json.path("name").asText(null)));
    }

    /**
     * New composed HTTP input.
     *
     * @param origin Validated input
     */
    private ThInputHttp(final TeacherInput origin) {
        this.origin = origin;
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return this.origin.print(media);
    }
}
