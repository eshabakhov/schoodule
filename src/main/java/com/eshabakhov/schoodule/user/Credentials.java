/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user;

import com.eshabakhov.schoodule.tables.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jooq.DSLContext;

/**
 * User credentials.
 *
 * @since 0.0.1
 */
public interface Credentials {

    /**
     * Username.
     *
     * @return Username
     */
    @JsonProperty
    String username();

    /**
     * Password.
     *
     * @return Password
     */
    String password();

    /**
     * Plain implementation of {@link Credentials}.
     *
     * @since 0.0.1
    */
    final class CdPostgres implements Credentials {

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
}
