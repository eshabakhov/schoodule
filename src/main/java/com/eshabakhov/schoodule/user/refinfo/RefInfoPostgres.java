/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user.refinfo;

import com.eshabakhov.schoodule.tables.User;
import com.eshabakhov.schoodule.user.ReferenceInformation;
import org.jooq.DSLContext;

/**
 * Postgres implementation of {@link ReferenceInformation}.
 *
 * @since 0.0.1
 */
public final class RefInfoPostgres implements ReferenceInformation {

    /** Database context. */
    private final DSLContext ctx;

    /** User id. */
    private final Long user;

    public RefInfoPostgres(final DSLContext ctx, final Long user) {
        this.ctx = ctx;
        this.user = user;
    }

    @Override
    public Long school() {
        return this.ctx.select(User.USER.SCHOOL_ID)
            .from(User.USER)
            .where(User.USER.ID.eq(this.user))
            .fetchOneInto(Long.class);
    }

    @Override
    public Boolean corporate() {
        return this.ctx.select(User.USER.CORPORATE)
            .from(User.USER)
            .where(User.USER.ID.eq(this.user))
            .fetchOneInto(Boolean.class);
    }

    @Override
    public Boolean alive() {
        return this.ctx.select(User.USER.DELETED)
            .from(User.USER)
            .where(User.USER.ID.eq(this.user))
            .fetchOne() != null;
    }
}
