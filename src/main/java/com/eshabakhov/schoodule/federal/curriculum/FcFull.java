/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum;

import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.federal.FederalCurriculum;
import com.eshabakhov.schoodule.federal.FederalCurriculumRequirements;

/**
 * Full implementation of {@link FederalCurriculum}.
 *
 * @since 0.0.1
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class FcFull implements FederalCurriculum {

    /**
     * Original federal curriculum.
     */
    private final FederalCurriculum origin;

    /**
     * Creates a Full curriculum.
     *
     * @param origin Original federal curriculum
     */
    public FcFull(final FederalCurriculum origin) {
        this.origin = origin;
    }

    @Override
    public Long uid() {
        return this.origin.uid();
    }

    @Override
    public Media print(final Media media) {
        return this.origin.print(media)
            .include("id", "title", "level", "week", "version", "year", "description");
    }

    @Override
    public FederalCurriculum retitled(final String title) {
        return this.origin.retitled(title);
    }

    @Override
    public FederalCurriculum releveled(final Level level) {
        return this.origin.releveled(level);
    }

    @Override
    public FederalCurriculum reweeked(final Week week) {
        return this.origin.reweeked(week);
    }

    @Override
    public FederalCurriculum reversioned(final String version) {
        return this.origin.reversioned(version);
    }

    @Override
    public FederalCurriculum reyeared(final String year) {
        return this.origin.reyeared(year);
    }

    @Override
    public FederalCurriculum redescriptioned(final String description) {
        return this.origin.redescriptioned(description);
    }

    @Override
    public FederalCurriculumRequirements requirements() {
        return this.origin.requirements();
    }
}
