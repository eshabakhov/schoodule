/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user;

import com.eshabakhov.schoodule.enums.RoleType;
import com.eshabakhov.schoodule.tables.UserRole;
import com.eshabakhov.schoodule.tables.records.RoleRecord;
import com.eshabakhov.schoodule.tables.records.UserRoleRecord;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;
import org.jooq.DSLContext;

/**
 * User roles collection abstraction.
 *
 * @since 0.0.1
 */
public interface Roles {

    /**
     * Assign role to user.
     *
     * @param role User role
     * @return Created user role
     * @throws Exception if assignment fails
     */
    Role grant(RoleEnum role) throws Exception;

    /**
     * Get all roles for user.
     *
     * @return List of user roles
     */
    @JsonProperty
    List<Role> list();

    /**
     * Remove role from user.
     *
     * @param identity User role ID
     * @throws Exception if removal fails
     */
    void revoke(long identity) throws Exception;

    /**
     * User role enumeration.
     *
     * @since 0.0.1
     */
    enum RoleEnum {

        /** System administrator. */
        ADMIN,

        /** School director. */
        DIRECTOR,

        /** Deputy director. */
        DEPUTY_DIRECTOR,

        /** Teacher. */
        TEACHER,

        /** Student. */
        STUDENT,

        /** Basic maker. */
        BASIC_MAKER,

        /** Advanced maker. */
        ADVANCED_MAKER,

        /** Pro maker. */
        PRO_MAKER,

        /** Viewer. */
        VIEWER
    }

    /**
     * PostgreSQL user roles implementation.
     *
     * <p>Manages user role assignments in PostgreSQL database.</p>
     *
     * @since 0.0.1
     */
    final class RlsPostgres implements Roles {

        /** JOOQ table for role. */
        // @checkstyle FullyQualifiedTypeCheck (2 lines)
        private static final com.eshabakhov.schoodule.tables.Role ROLE =
            com.eshabakhov.schoodule.tables.Role.ROLE;

        /** Database context. */
        private final DSLContext ctx;

        /** User. */
        private final Long user;

        /**
         * New roles collection.
         *
         * @param ctx Database context
         * @param user User identifier
         */
        public RlsPostgres(final DSLContext ctx, final Long user) {
            this.ctx = ctx;
            this.user = user;
        }

        @Override
        public Role grant(final RoleEnum role) throws Exception {
            final RoleRecord selected = this.ctx.selectFrom(Roles.RlsPostgres.ROLE)
                .where(Roles.RlsPostgres.ROLE.NAME.eq(RoleType.valueOf(role.name())))
                .fetchOne();
            if (selected == null) {
                throw new RoleNotFoundException(
                    String.format("Role %s not found", role.name())
                );
            }
            final UserRoleRecord created = this.ctx.insertInto(UserRole.USER_ROLE)
                .set(UserRole.USER_ROLE.USER_ID, this.user)
                .set(UserRole.USER_ROLE.ROLE_ID, selected.getId())
                .returning()
                .fetchOne();
            if (created == null) {
                throw new RoleGrantException("Failed to grant role");
            }
            return new Role.RlPostgres(this.ctx, created.getId());
        }

        @Override
        public List<Role> list() {
            return this.ctx.select(
                UserRole.USER_ROLE.ID,
                UserRole.USER_ROLE.USER_ID,
                Roles.RlsPostgres.ROLE.NAME
                )
                .from(UserRole.USER_ROLE)
                .join(Roles.RlsPostgres.ROLE)
                .on(UserRole.USER_ROLE.ROLE_ID.eq(Roles.RlsPostgres.ROLE.ID))
                .where(UserRole.USER_ROLE.USER_ID.eq(this.user))
                .fetch()
                .stream()
                .map(role -> new Role.RlPostgres(this.ctx, role.get(UserRole.USER_ROLE.ID)))
                .collect(Collectors.toList());
        }

        @Override
        public void revoke(final long identity) {
            this.ctx.deleteFrom(UserRole.USER_ROLE)
                .where(UserRole.USER_ROLE.ID.eq(identity))
                .execute();
        }
    }
}
