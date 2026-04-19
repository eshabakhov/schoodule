/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user;

import com.eshabakhov.schoodule.enums.RoleType;
import com.eshabakhov.schoodule.tables.Role;
import com.eshabakhov.schoodule.tables.SubscriptionPlanRoles;
import com.eshabakhov.schoodule.tables.User;
import com.eshabakhov.schoodule.tables.UserRole;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Nightly job that resets expired personal-user subscriptions to BASIC
 * and downgrades their subscription-managed role sets accordingly.
 *
 * <p>Requires {@code @EnableScheduling} on the application class.</p>
 * <p>Schedule: every day at 03:00 UTC.</p>
 *
 * <p>Usage example:
 * <pre>
 * // Triggered automatically by Spring Scheduler; no manual invocation needed.
 * </pre>
 * </p>
 *
 * @since 0.0.1
 */
@Component
public final class ExpiryReset {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(ExpiryReset.class);

    /** JOOQ table reference for subscription. */
    private static final com.eshabakhov.schoodule.tables.Subscription SUB =
        com.eshabakhov.schoodule.tables.Subscription.SUBSCRIPTION;

    /** JOOQ table reference for subscription_plan_roles. */
    private static final SubscriptionPlanRoles SPR = SubscriptionPlanRoles.SUBSCRIPTION_PLAN_ROLES;

    /** Database context. */
    private final DSLContext ctx;

    public ExpiryReset(final DSLContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Runs at 03:00 UTC every day and downgrades all expired subscriptions.
     */
    @Scheduled(cron = "0 0 3 * * *", zone = "UTC")
    public void reset() {
        final var expired = this.ctx
            .select(ExpiryReset.SUB.USER_ID)
            .from(ExpiryReset.SUB)
            .join(User.USER)
            .on(User.USER.ID.eq(ExpiryReset.SUB.USER_ID))
            .where(
                ExpiryReset.SUB.PLAN.ne("BASIC")
                    .and(ExpiryReset.SUB.EXPIRES_AT.isNotNull())
                    .and(ExpiryReset.SUB.EXPIRES_AT.lt(Instant.now().atOffset(ZoneOffset.UTC)))
                    .and(User.USER.CORPORATE.eq(false))
                    .and(User.USER.DELETED.isNull())
            )
            .fetch();
        final int size;
        if (expired.isEmpty()) {
            size = 0;
        } else {
            final List<RoleType> basic = roles(this.ctx, SubscriptionPlan.BASIC);
            for (final var row : expired) {
                final long uid = row.get(ExpiryReset.SUB.USER_ID);
                this.ctx.transaction(
                    conf -> {
                        final var dsl = conf.dsl();
                        swap(dsl, uid, basic);
                        dsl.update(ExpiryReset.SUB)
                            .set(ExpiryReset.SUB.PLAN, "BASIC")
                            .setNull(ExpiryReset.SUB.EXPIRES_AT)
                            .set(
                                ExpiryReset.SUB.UPDATED_AT,
                                Instant.now().atOffset(ZoneOffset.UTC)
                            )
                            .where(ExpiryReset.SUB.USER_ID.eq(uid))
                            .execute();
                    }
                );
            }
            size = expired.size();
        }
        if (size > 0) {
            LOG.info(
                "[ExpiryReset] Downgraded {} expired subscription(s) to BASIC%n",
                size
            );
        }
    }

    /**
     * Reads the {@link RoleType} set for the given plan
     * from {@code subscription_plan_roles}.
     *
     * @param dsl  DSL context (may be inside a transaction)
     * @param plan Target plan
     * @return List of role types to grant
     */
    private static List<RoleType> roles(final DSLContext dsl, final SubscriptionPlan plan) {
        return dsl.select(ExpiryReset.SPR.ROLE_NAME)
            .from(ExpiryReset.SPR)
            .where(ExpiryReset.SPR.PLAN.eq(plan.name()))
            .fetch()
            .stream()
            .map(r -> r.get(ExpiryReset.SPR.ROLE_NAME))
            .toList();
    }

    /**
     * Removes all subscription-managed roles from the user then inserts
     * the new set. Corporate roles are never in the managed list and
     * are therefore never touched.
     *
     * @param dsl    DSL context (must be inside a transaction)
     * @param user   User identifier
     * @param grants Role types to grant
     */
    private static void swap(
        final DSLContext dsl,
        final long user,
        final List<RoleType> grants
    ) {
        dsl.deleteFrom(UserRole.USER_ROLE)
            .where(
                UserRole.USER_ROLE.USER_ID.eq(user)
                    .and(
                        UserRole.USER_ROLE.ROLE_ID.in(
                            dsl.select(Role.ROLE.ID)
                                .from(Role.ROLE)
                                .where(
                                    Role.ROLE.NAME.in(
                                        List.of(
                                            "BASIC_MAKER", "ADVANCED_MAKER", "PRO_MAKER", "VIEWER"
                                        )
                                    )
                                )
                        )
                    )
            )
            .execute();
        for (final RoleType role : grants) {
            final var rid = dsl
                .select(Role.ROLE.ID)
                .from(Role.ROLE)
                .where(Role.ROLE.NAME.eq(role))
                .fetchOne();
            if (rid == null) {
                throw new IllegalStateException(
                    String.format("Role not found in database (name=%s)", role.getLiteral())
                );
            }
            dsl.insertInto(UserRole.USER_ROLE)
                .set(UserRole.USER_ROLE.USER_ID, user)
                .set(UserRole.USER_ROLE.ROLE_ID, rid.get(Role.ROLE.ID))
                .onConflict(UserRole.USER_ROLE.USER_ID, UserRole.USER_ROLE.ROLE_ID)
                .doNothing()
                .execute();
        }
    }
}
