/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user;

import com.eshabakhov.schoodule.User;
import org.jooq.DSLContext;

/**
 * Simple user implementation.
 *
 * @since 0.0.1
 */
public final class UrPostgres implements User {

    /** Database context. */
    private final DSLContext ctx;

    /** User ID. */
    private final long id;

    public UrPostgres(final DSLContext ctx, final long id) {
        this.ctx = ctx;
        this.id = id;
    }

    @Override
    public long uid() {
        return this.id;
    }

    @Override
    public Credentials credentials() {
        return new Credentials.CdPostgres(this.ctx, this.id);
    }

    @Override
    public Roles roles() {
        return new Roles.RlsPostgres(this.ctx, this.id);
    }

    @Override
    public ReferenceInformation info() {
        return new ReferenceInformation.RefInfoPostgres(this.ctx, this.id);
    }

    @Override
    public boolean isAdmin() {
        return new Roles.RlsPostgres(this.ctx, this.id)
            .list()
            .stream()
            .anyMatch(role -> "ADMIN".equalsIgnoreCase(role.name()));
    }
}
