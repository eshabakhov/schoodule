/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.user;

/**
 * In-memory user role assignment.
 *
 * @since 0.0.1
 */
public final class RlFake implements Role {

    /** Role assignment identity. */
    private final long identity;

    /** Role name. */
    private final String title;

    /**
     * New in-memory role assignment.
     *
     * @param identity Role assignment identity
     * @param title Role name
     */
    public RlFake(final long identity, final String title) {
        this.identity = identity;
        this.title = title;
    }

    @Override
    public long uid() {
        return this.identity;
    }

    @Override
    public String name() {
        return this.title;
    }
}
