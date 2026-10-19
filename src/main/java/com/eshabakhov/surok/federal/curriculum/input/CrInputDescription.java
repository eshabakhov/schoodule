/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.federal.curriculum.input;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.federal.curriculum.CurriculumInput;

/**
 * Federal curriculum input with a description.
 *
 * @since 0.0.1
 */
public final class CrInputDescription implements CurriculumInput {

    /** Origin input. */
    private final CurriculumInput origin;

    /** Curriculum description. */
    private final String description;

    /**
     * New description input.
     *
     * @param origin Origin input
     * @param description Curriculum description
     */
    public CrInputDescription(
        final CurriculumInput origin,
        final String description
    ) {
        this.origin = origin;
        this.description = description;
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        String value = this.description;
        if (value != null) {
            value = value.trim();
        }
        return this.origin.print(media).with("description", value);
    }
}
