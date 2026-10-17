/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.input;

import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.federal.curriculum.CurriculumInput;

/**
 * Federal curriculum input with a version.
 *
 * @since 0.1.0
 */
public final class CrInputVersion implements CurriculumInput {

    /** Origin input. */
    private final CurriculumInput origin;

    /** Curriculum version. */
    private final String version;

    /**
     * New version input.
     *
     * @param origin Origin input
     * @param version Curriculum version
     */
    public CrInputVersion(
        final CurriculumInput origin,
        final String version
    ) {
        this.origin = origin;
        this.version = version;
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        if (this.version == null || this.version.isBlank()) {
            throw new CurriculumInputException(
                "Curriculum version is required"
            );
        }
        return this.origin.print(media).with(
            "version", this.version.trim()
        );
    }
}
