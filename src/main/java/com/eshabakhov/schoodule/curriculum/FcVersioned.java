/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.curriculum;

import com.eshabakhov.schoodule.Media;
import lombok.EqualsAndHashCode;

/**
 * Decorator that adds a version to a {@link FederalCurriculum}.
 *
 * @since 0.0.1
 */
@EqualsAndHashCode(of = {"origin", "version"})
public final class FcVersioned implements FederalCurriculum {

    /**
     * Wrapped curriculum.
     */
    private final FederalCurriculum origin;

    /**
     * Version.
     */
    private final String version;

    /**
     * Creates a versioned curriculum decorator.
     *
     * @param origin  Wrapped curriculum
     * @param version Version string
     */
    public FcVersioned(final FederalCurriculum origin, final String version) {
        this.origin = origin;
        this.version = version;
    }

    @Override
    public Long uid() {
        return this.origin.uid();
    }

    @Override
    public Media print(final Media media) {
        return this.origin.print(media).with("version", this.version);
    }

    @Override
    public FederalCurriculum retitled(final String title) {
        return new FcTitled(this, title);
    }

    @Override
    public FederalCurriculum releveled(final Level level) {
        return new FcVersioned(this.origin.releveled(level), this.version);
    }

    @Override
    public FederalCurriculum reweeked(final Week week) {
        return new FcVersioned(this.origin.reweeked(week), this.version);
    }

    @Override
    public FederalCurriculum reversioned(final String ver) {
        return new FcVersioned(this.origin, ver);
    }

    @Override
    public FederalCurriculum reyeared(final String year) {
        return new FcYeared(this, year);
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
