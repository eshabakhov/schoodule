/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.input;

import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.federal.curriculum.CurriculumInput;

/**
 * Federal curriculum input with a title.
 *
 * @since 0.0.1
 */
public final class CrInputTitle implements CurriculumInput {

    /** Origin input. */
    private final CurriculumInput origin;

    /** Curriculum title. */
    private final String title;

    /**
     * New title input.
     *
     * @param origin Origin input
     * @param title Curriculum title
     */
    public CrInputTitle(final CurriculumInput origin, final String title) {
        this.origin = origin;
        this.title = title;
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        if (this.title == null || this.title.isBlank()) {
            throw new CurriculumInputException(
                "Curriculum title is required"
            );
        }
        return this.origin.print(media).with("title", this.title.trim());
    }
}
