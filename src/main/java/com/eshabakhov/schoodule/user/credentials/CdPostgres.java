/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user.credentials;

import com.eshabakhov.schoodule.tables.User;
import com.eshabakhov.schoodule.user.Credentials;
import org.jooq.DSLContext;

/**
 * Plain implementation of {@link Credentials}.
 *
 * @since 0.0.1
 */
public final class CdPostgres implements Credentials {

    /** Username. */
    private final DSLContext ctx;

    /** Password. */
    private final Long user;

    public CdPostgres(final DSLContext ctx, final Long user) {
        this.ctx = ctx;
        this.user = user;
    }

    @Override
    public String username() {
        return this.ctx.select(User.USER.USERNAME)
            .from(User.USER)
            .where(User.USER.ID.eq(this.user))
            .fetchOneInto(String.class);
    }

    @Override
    public String password() {
        return this.ctx.select(User.USER.PASSWORD)
            .from(User.USER)
            .where(User.USER.ID.eq(this.user))
            .fetchOneInto(String.class);
    }
}
