/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user.subscription;

import com.eshabakhov.schoodule.enums.RoleType;
import com.eshabakhov.schoodule.tables.Role;
import com.eshabakhov.schoodule.tables.SubscriptionPlanRoles;
import com.eshabakhov.schoodule.tables.UserRole;
import com.eshabakhov.schoodule.user.AuthUser;
import com.eshabakhov.schoodule.user.Subscription;
import com.eshabakhov.schoodule.user.Subscriptions;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Record2;

/**
 * PostgreSQL-backed implementation of {@link Subscriptions}.
 *
 * <p>Each mutating operation (activate, downgrade) runs inside a single
 * jOOQ transaction that atomically swaps roles in {@code user_role} and
 * updates the {@code subscription} row. Corporate users are rejected
 * before any database work is done.</p>
 * Usage example:
 * <pre>
 * final Subscriptions subs = new PgSubscriptions(dsl);
 * subs.activate(userId, SubscriptionPlan.PRO);
 * </pre>
 *
 * @since 0.0.1
 */
public final class PgSubscriptions implements Subscriptions {

    /** JOOQ table reference for subscription. */
    private static final com.eshabakhov.schoodule.tables.Subscription SUB =
        com.eshabakhov.schoodule.tables.Subscription.SUBSCRIPTION;

    /** JOOQ table reference for subscription_plan_roles. */
    private static final SubscriptionPlanRoles SPR =
        SubscriptionPlanRoles.SUBSCRIPTION_PLAN_ROLES;

    /** JOOQ DSL context for all database operations. */
    private final DSLContext ctx;

    /** Authenticated user. */
    private final AuthUser user;

    /**
     * New subscriptions collection.
     *
     * @param ctx Database context
     * @param user Authenticated user
     */
    public PgSubscriptions(final DSLContext ctx, final AuthUser user) {
        this.ctx = ctx;
        this.user = user;
    }

    @Override
    public Subscription subscription() throws Exception {
        if (this.user.info().corporate() || this.user.isAdmin()) {
            throw new PersonalOnlyException(this.user.uid());
        }
        final Record2<String, OffsetDateTime> selected = this.ctx.select(
            PgSubscriptions.SUB.PLAN, PgSubscriptions.SUB.EXPIRES_AT
            )
            .from(PgSubscriptions.SUB)
            .where(PgSubscriptions.SUB.USER_ID.eq(this.user.uid()))
            .fetchOne();
        if (selected == null) {
            this.ctx.insertInto(PgSubscriptions.SUB)
                .set(PgSubscriptions.SUB.USER_ID, this.user.uid())
                .set(PgSubscriptions.SUB.PLAN, "BASIC")
                .setNull(PgSubscriptions.SUB.EXPIRES_AT)
                .onConflict(PgSubscriptions.SUB.USER_ID)
                .doNothing()
                .execute();
        }
        return new PgSubscription(this.ctx, this.user.uid());
    }

    @Override
    public void subscription(final Subscription.Plan plan) throws Exception {
        if (this.user.info().corporate() || this.user.isAdmin()) {
            throw new PersonalOnlyException(this.user.uid());
        }
        final OffsetDateTime expires;
        if (plan.free()) {
            expires = null;
        } else {
            expires = OffsetDateTime.now(ZoneOffset.UTC).plusDays(30);
        }
        this.ctx.transaction(
            conf -> {
                final DSLContext ttx = conf.dsl();
                this.swap(ttx, PgSubscriptions.roles(ttx, plan));
                ttx.insertInto(PgSubscriptions.SUB)
                    .set(PgSubscriptions.SUB.USER_ID, this.user.uid())
                    .set(PgSubscriptions.SUB.PLAN, plan.name())
                    .set(PgSubscriptions.SUB.EXPIRES_AT, expires)
                    .set(PgSubscriptions.SUB.UPDATED_AT, Instant.now().atOffset(ZoneOffset.UTC))
                    .onConflict(PgSubscriptions.SUB.USER_ID)
                    .doUpdate()
                    .set(PgSubscriptions.SUB.PLAN, plan.name())
                    .set(PgSubscriptions.SUB.EXPIRES_AT, expires)
                    .set(PgSubscriptions.SUB.UPDATED_AT, Instant.now().atOffset(ZoneOffset.UTC))
                    .execute();
            }
        );
    }

    private static List<RoleType> roles(final DSLContext dsl, final Subscription.Plan plan) {
        return dsl.select(PgSubscriptions.SPR.ROLE_NAME)
            .from(PgSubscriptions.SPR)
            .where(PgSubscriptions.SPR.PLAN.eq(plan.name()))
            .fetch()
            .stream()
            .map(r -> r.get(PgSubscriptions.SPR.ROLE_NAME))
            .toList();
    }

    private void swap(final DSLContext dsl, final List<RoleType> grants) {
        dsl.deleteFrom(UserRole.USER_ROLE).where(
            UserRole.USER_ROLE.USER_ID.eq(this.user.uid()).and(
                UserRole.USER_ROLE.ROLE_ID.in(
                    dsl.select(Role.ROLE.ID).from(Role.ROLE).where(
                        Role.ROLE.NAME.in(
                            List.of(
                                "BASIC_MAKER", "ADVANCED_MAKER", "PRO_MAKER", "VIEWER"
                            )
                        )
                    )
                )
            )
        ).execute();
        for (final RoleType role : grants) {
            final Record1<Long> rid = dsl.select(Role.ROLE.ID)
                .from(Role.ROLE)
                .where(Role.ROLE.NAME.eq(role))
                .fetchOne();
            if (rid == null) {
                throw new IllegalStateException(
                    String.format("Role not found in database (name=%s)", role.getLiteral())
                );
            }
            dsl.insertInto(UserRole.USER_ROLE)
                .set(UserRole.USER_ROLE.USER_ID, this.user.uid())
                .set(UserRole.USER_ROLE.ROLE_ID, rid.get(Role.ROLE.ID))
                .onConflict(UserRole.USER_ROLE.USER_ID, UserRole.USER_ROLE.ROLE_ID)
                .doNothing()
                .execute();
        }
    }
}
