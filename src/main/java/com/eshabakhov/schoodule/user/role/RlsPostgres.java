/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user.role;

import com.eshabakhov.schoodule.enums.RoleType;
import com.eshabakhov.schoodule.user.Role;
import com.eshabakhov.schoodule.user.RoleEnum;
import com.eshabakhov.schoodule.user.Roles;
import java.util.List;
import java.util.stream.Collectors;
import org.jooq.DSLContext;

/**
 * PostgreSQL user roles implementation.
 *
 * <p>Manages user role assignments in PostgreSQL database.</p>
 *
 * @since 0.0.1
 */
public final class RlsPostgres implements Roles {

    /** JOOQ Table for Role. */
    private static final com.eshabakhov.schoodule.tables.Role ROLE =
        com.eshabakhov.schoodule.tables.Role.ROLE;

    /** JOOQ Table for UserRole. */
    private static final com.eshabakhov.schoodule.tables.UserRole USER_ROLE =
        com.eshabakhov.schoodule.tables.UserRole.USER_ROLE;

    /** Database context. */
    private final DSLContext ctx;

    /** User. */
    private final Long user;

    public RlsPostgres(final DSLContext ctx, final Long user) {
        this.ctx = ctx;
        this.user = user;
    }

    @Override
    public Role grant(final RoleEnum role) throws Exception {
        final var selected = this.ctx.selectFrom(RlsPostgres.ROLE)
            .where(RlsPostgres.ROLE.NAME.eq(RoleType.valueOf(role.name())))
            .fetchOne();
        if (selected == null) {
            throw new RoleNotFoundException(
                String.format("Role %s not found", role.name())
            );
        }
        final var created = this.ctx.insertInto(RlsPostgres.USER_ROLE)
            .set(RlsPostgres.USER_ROLE.USER_ID, this.user)
            .set(RlsPostgres.USER_ROLE.ROLE_ID, selected.getId())
            .returning()
            .fetchOne();
        if (created == null) {
            throw new RoleGrantException("Failed to grant role");
        }
        return new RlBase(created.getId(), role.name());
    }

    @Override
    public List<Role> list() {
        return this.ctx
            .select(
                RlsPostgres.USER_ROLE.ID,
                RlsPostgres.USER_ROLE.USER_ID,
                RlsPostgres.ROLE.NAME
            )
            .from(RlsPostgres.USER_ROLE)
            .join(RlsPostgres.ROLE)
            .on(RlsPostgres.USER_ROLE.ROLE_ID.eq(RlsPostgres.ROLE.ID))
            .where(RlsPostgres.USER_ROLE.USER_ID.eq(this.user))
            .fetch()
            .stream()
            .map(
                role -> new RlBase(
                    role.get(RlsPostgres.USER_ROLE.ID),
                    role.get(RlsPostgres.ROLE.NAME).getLiteral()
                )
            )
            .collect(Collectors.toList());
    }

    @Override
    public void revoke(final long identity) {
        this.ctx.deleteFrom(RlsPostgres.USER_ROLE)
            .where(RlsPostgres.USER_ROLE.ID.eq(identity))
            .execute();
    }

    /**
     * Role not found exception.
     *
     * @since 0.0.1
     */
    public static class RoleNotFoundException extends Exception {

        public RoleNotFoundException(final String message) {
            super(message);
        }
    }

    /**
     * Role assign exception.
     *
     * @since 0.0.1
     */
    public static class RoleGrantException extends Exception {

        public RoleGrantException(final String message) {
            super(message);
        }
    }
}
