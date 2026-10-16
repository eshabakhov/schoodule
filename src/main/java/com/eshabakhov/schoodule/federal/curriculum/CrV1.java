/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum;

import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.federal.curriculum.requirement.Requirements;

/**
 * Version 1 implementation of {@link Curriculum}.
 *
 * @since 0.0.1
 */
public final class CrV1 implements Curriculum {

    /**
     * Original federal curriculum.
     */
    private final Curriculum origin;

    /**
     * Creates a version 1 curriculum.
     *
     * @param origin Original federal curriculum
     */
    public CrV1(final Curriculum origin) {
        this.origin = origin;
    }

    @Override
    public Long uid() {
        return this.origin.uid();
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return this.origin.print(media).include(
            "id", "title", "level", "week", "version", "year"
        );
    }

    @Override
    public Curriculum retitled(final String title) {
        return this.origin.retitled(title);
    }

    @Override
    public Curriculum releveled(final Level level) {
        return this.origin.releveled(level);
    }

    @Override
    public Curriculum reweeked(final Week week) {
        return this.origin.reweeked(week);
    }

    @Override
    public Curriculum reversioned(final String version) {
        return this.origin.reversioned(version);
    }

    @Override
    public Curriculum reyeared(final String year) {
        return this.origin.reyeared(year);
    }

    @Override
    public Curriculum redescriptioned(final String description) {
        return this.origin.redescriptioned(description);
    }

    @Override
    public Requirements requirements() {
        return this.origin.requirements();
    }
}
