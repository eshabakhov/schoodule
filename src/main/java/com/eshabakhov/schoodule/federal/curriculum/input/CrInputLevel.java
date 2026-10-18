/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.input;

import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.federal.curriculum.Curriculum;
import com.eshabakhov.schoodule.federal.curriculum.CurriculumInput;

/**
 * Federal curriculum input with an education level.
 *
 * @since 0.0.1
 */
public final class CrInputLevel implements CurriculumInput {

    /** Origin input. */
    private final CurriculumInput origin;

    /** Education level. */
    private final String level;

    /**
     * New education-level input.
     *
     * @param origin Origin input
     * @param level Education level
     */
    public CrInputLevel(final CurriculumInput origin, final String level) {
        this.origin = origin;
        this.level = level;
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        if (this.level == null || this.level.isBlank()) {
            throw new CurriculumInputException(
                "Curriculum education level is required"
            );
        }
        try {
            return this.origin.print(media).with(
                "level",
                Curriculum.Level.valueOf(this.level.trim()).name()
            );
        } catch (final IllegalArgumentException exception) {
            throw new CurriculumInputException(
                String.format(
                    "Unsupported curriculum education level `%s`",
                    this.level
                ),
                exception
            );
        }
    }
}
