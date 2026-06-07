/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.curriculum;

import com.eshabakhov.schoodule.Media;
import lombok.EqualsAndHashCode;

/**
 * Decorator that adds a title to a {@link FederalCurriculum}.
 *
 * @since 0.0.1
 */
@EqualsAndHashCode(of = {"origin", "title"})
public final class FcTitled implements FederalCurriculum {

    /**
     * Wrapped curriculum.
     */
    private final FederalCurriculum origin;

    /**
     * Title.
     */
    private final String title;

    /**
     * Creates a titled curriculum decorator.
     *
     * @param origin Wrapped curriculum
     * @param title  Title
     */
    public FcTitled(final FederalCurriculum origin, final String title) {
        this.origin = origin;
        this.title = title;
    }

    @Override
    public Long uid() {
        return this.origin.uid();
    }

    @Override
    public Media print(final Media media) {
        return this.origin.print(media).with("title", this.title);
    }

    @Override
    public FederalCurriculum retitled(final String ttl) {
        return new FcTitled(this.origin, ttl);
    }

    @Override
    public FederalCurriculum releveled(final Level level) {
        return new FcTitled(this.origin.releveled(level), this.title);
    }

    @Override
    public FederalCurriculum reweeked(final Week week) {
        return new FcTitled(this.origin.reweeked(week), this.title);
    }

    @Override
    public FederalCurriculum reversioned(final String version) {
        return new FcVersioned(this, version);
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
