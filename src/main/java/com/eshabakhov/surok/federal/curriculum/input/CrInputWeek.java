/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.federal.curriculum.input;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.federal.curriculum.Curriculum;
import com.eshabakhov.surok.federal.curriculum.CurriculumInput;

/**
 * Federal curriculum input with a study week.
 *
 * @since 0.0.1
 */
public final class CrInputWeek implements CurriculumInput {

    /** Origin input. */
    private final CurriculumInput origin;

    /** Study week. */
    private final String week;

    /**
     * New study-week input.
     *
     * @param origin Origin input
     * @param week Study week
     */
    public CrInputWeek(final CurriculumInput origin, final String week) {
        this.origin = origin;
        this.week = week;
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        if (this.week == null || this.week.isBlank()) {
            throw new CurriculumInputException(
                "Curriculum study week is required"
            );
        }
        try {
            return this.origin.print(media).with(
                "week",
                Curriculum.Week.valueOf(this.week.trim()).name()
            );
        } catch (final IllegalArgumentException exception) {
            throw new CurriculumInputException(
                String.format(
                    "Unsupported curriculum study week `%s`",
                    this.week
                ),
                exception
            );
        }
    }
}
