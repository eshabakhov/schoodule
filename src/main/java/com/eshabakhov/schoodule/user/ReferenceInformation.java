/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user;

import com.eshabakhov.schoodule.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jooq.DSLContext;

/**
 * Reference information for {@link User}.
 *
 * @since 0.0.1
 */
public interface ReferenceInformation {

    @JsonProperty
    Long school();

    @JsonProperty
    Boolean corporate();

    @JsonProperty
    Boolean alive();

    /**
     * Postgres implementation of {@link ReferenceInformation}.
     *
     * @since 0.0.1
     */
    final class RefInfoPostgres implements ReferenceInformation {

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
            return this.ctx.select(com.eshabakhov.schoodule.tables.User.USER.SCHOOL_ID)
                .from(com.eshabakhov.schoodule.tables.User.USER)
                .where(com.eshabakhov.schoodule.tables.User.USER.ID.eq(this.user))
                .fetchOneInto(Long.class);
        }

        @Override
        public Boolean corporate() {
            return this.ctx.select(com.eshabakhov.schoodule.tables.User.USER.CORPORATE)
                .from(com.eshabakhov.schoodule.tables.User.USER)
                .where(com.eshabakhov.schoodule.tables.User.USER.ID.eq(this.user))
                .fetchOneInto(Boolean.class);
        }

        @Override
        public Boolean alive() {
            return this.ctx.select(com.eshabakhov.schoodule.tables.User.USER.DELETED)
                .from(com.eshabakhov.schoodule.tables.User.USER)
                .where(com.eshabakhov.schoodule.tables.User.USER.ID.eq(this.user))
                .fetchOne() != null;
        }
    }

}
