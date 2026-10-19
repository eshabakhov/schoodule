/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.federal.curriculum.requirement;

import com.eshabakhov.surok.Media;

/**
 * In-memory federal curriculum requirement.
 *
 * @since 0.0.1
 */
public final class RqFake implements Requirement {

    /** Requirement identity. */
    private final long identity;

    /** Requirement representation. */
    private final RequirementInput input;

    /**
     * New in-memory requirement.
     *
     * @param identity Requirement identity
     * @param input Requirement input
     */
    public RqFake(final long identity, final RequirementInput input) {
        this.identity = identity;
        this.input = input;
    }

    @Override
    public Long uid() {
        return this.identity;
    }

    @Override
    public Requirement revise(final RequirementInput revised) {
        return new RqFake(this.identity, revised);
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return this.input.print(media.with("id", this.identity)).with(
            "internal", "fake"
        );
    }
}
