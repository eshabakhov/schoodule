/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum;

import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.federal.curriculum.requirement.Requirements;

/**
 * Curriculum representation with its requirements.
 *
 * @since 0.0.1
 */
public final class CrWithRequirements implements Curriculum {

    /** Origin curriculum. */
    private final Curriculum origin;

    /** Curriculum requirements. */
    private final Requirements requirements;

    /**
     * New curriculum representation.
     *
     * @param origin Origin curriculum
     * @param requirements Curriculum requirements
     */
    public CrWithRequirements(
        final Curriculum origin,
        final Requirements requirements
    ) {
        this.origin = origin;
        this.requirements = requirements;
    }

    @Override
    public Long uid() {
        return this.origin.uid();
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
        return this.requirements;
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return this.origin.print(media).with(
            "requirements", this.requirements
        );
    }
}
