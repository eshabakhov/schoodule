/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.user;

import com.eshabakhov.surok.sql.ClasspathSql;
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

        /**
         * New credentials.
         *
         * @param ctx Database context
         * @param user User identifier
         */
        public CdPostgres(final DSLContext ctx, final Long user) {
            this.ctx = ctx;
            this.user = user;
        }

        @Override
        public String username() {
            return this.ctx.resultQuery(
                new ClasspathSql("user/find-username.sql").text(),
                this.user
            ).fetchOneInto(String.class);
        }

        @Override
        public String password() {
            return this.ctx.resultQuery(
                new ClasspathSql("user/find-password.sql").text(),
                this.user
            ).fetchOneInto(String.class);
        }
    }
}
