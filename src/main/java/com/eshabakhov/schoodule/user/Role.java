/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jooq.DSLContext;

/**
 * User role assignment abstraction.
 *
 * @since 0.0.1
 */
public interface Role {

    /**
     * User role assignment unique identifier.
     *
     * @return UserRole ID
     */
    @JsonProperty
    long uid();

    /**
     * Role name.
     *
     * @return Role name
     */
    @JsonProperty
    String name();

    /**
     * Simple user role implementation.
     *
     * <p>Immutable object representing user role assignment.</p>
     *
     * @since 0.0.1
     */
    final class RlPostgres implements Role {

        /** JOOQ Table for Role. */
        private static final com.eshabakhov.schoodule.tables.Role ROLE =
            com.eshabakhov.schoodule.tables.Role.ROLE;

        /** JOOQ Table for UserRole. */
        private static final com.eshabakhov.schoodule.tables.UserRole USER_ROLE =
            com.eshabakhov.schoodule.tables.UserRole.USER_ROLE;

        /** Database context. */
        private final DSLContext ctx;

        /** User role ID. */
        private final long id;

        public RlPostgres(final DSLContext ctx, final long id) {
            this.ctx = ctx;
            this.id = id;
        }

        @Override
        public long uid() {
            return this.id;
        }

        @Override
        public String name() {
            return this.ctx.select(Role.RlPostgres.ROLE.NAME)
                .from(Role.RlPostgres.ROLE)
                .join(Role.RlPostgres.USER_ROLE)
                .on(Role.RlPostgres.ROLE.ID.eq(Role.RlPostgres.USER_ROLE.ROLE_ID))
                .where(Role.RlPostgres.USER_ROLE.ID.eq(this.id))
                .fetchOneInto(String.class);
        }
    }
}
