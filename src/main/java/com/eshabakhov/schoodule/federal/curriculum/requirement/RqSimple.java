/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement;

import com.eshabakhov.schoodule.Media;

/**
 * Simple implementation of {@link Requirement}.
 *
 * @since 0.0.1
 */
public final class RqSimple implements Requirement {

    /**
     * Original federal curriculum requirement.
     */
    private final Requirement origin;

    /**
     * Creates a Postgres-backed requirement.
     *
     * @param origin Original federal curriculum requirement
     */
    public RqSimple(final Requirement origin) {
        this.origin = origin;
    }

    @Override
    public Long uid() {
        return this.origin.uid();
    }

    @Override
    public <M extends Media> M print(final M media) {
        this.origin.print(media)
            .include("id", "grade", "subjectName", "weeklyHours", "partType");
        return media;
    }

    @Override
    public Requirement regraded(final Integer grade) {
        return this.origin.regraded(grade);
    }

    @Override
    public Requirement resubjected(final String subject) {
        return this.origin.resubjected(subject);
    }

    @Override
    public Requirement reweekled(final Integer hours) {
        return this.origin.reweekled(hours);
    }

    @Override
    public Requirement reparted(final PartType part) {
        return this.origin.reparted(part);
    }
}
