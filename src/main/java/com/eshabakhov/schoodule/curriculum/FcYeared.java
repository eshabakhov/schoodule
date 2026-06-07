/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.curriculum;

import com.eshabakhov.schoodule.Media;
import lombok.EqualsAndHashCode;

/**
 * Decorator that adds an academic year to a {@link FederalCurriculum}.
 *
 * @since 0.0.1
 */
@EqualsAndHashCode(of = {"origin", "year"})
public final class FcYeared implements FederalCurriculum {

    /**
     * Wrapped curriculum.
     */
    private final FederalCurriculum origin;

    /**
     * Academic year (format: YYYY/YYYY).
     */
    private final String year;

    /**
     * Creates a yeared curriculum decorator.
     *
     * @param origin Wrapped curriculum
     * @param year   Academic year string, e.g. "2026/2027"
     */
    public FcYeared(final FederalCurriculum origin, final String year) {
        this.origin = origin;
        this.year = year;
    }

    @Override
    public Long uid() {
        return this.origin.uid();
    }

    @Override
    public Media print(final Media media) {
        return this.origin.print(media).with("year", this.year);
    }

    @Override
    public FederalCurriculum retitled(final String title) {
        return new FcTitled(this, title);
    }

    @Override
    public FederalCurriculum releveled(final Level level) {
        return new FcYeared(this.origin.releveled(level), this.year);
    }

    @Override
    public FederalCurriculum reweeked(final Week week) {
        return new FcYeared(this.origin.reweeked(week), this.year);
    }

    @Override
    public FederalCurriculum reversioned(final String version) {
        return new FcVersioned(this, version);
    }

    @Override
    public FederalCurriculum reyeared(final String yrr) {
        return new FcYeared(this.origin, yrr);
    }

    @Override
    public FederalCurriculum redescriptioned(final String description) {
        return new FcDescription(this, description);
    }

    @Override
    public FederalCurriculumRequirements requirements() {
        return this.origin.requirements();
    }
}
