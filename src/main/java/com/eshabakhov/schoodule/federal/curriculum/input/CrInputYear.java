/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.input;

import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.federal.curriculum.CurriculumInput;

/**
 * Federal curriculum input with an academic year.
 *
 * @since 0.1.0
 */
public final class CrInputYear implements CurriculumInput {

    /** Origin input. */
    private final CurriculumInput origin;

    /** Academic year. */
    private final String year;

    /**
     * New academic-year input.
     *
     * @param origin Origin input
     * @param year Academic year
     */
    public CrInputYear(final CurriculumInput origin, final String year) {
        this.origin = origin;
        this.year = year;
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        if (this.year == null || this.year.isBlank()) {
            throw new CurriculumInputException(
                "Curriculum academic year is required"
            );
        }
        return this.origin.print(media).with("year", this.year.trim());
    }
}
