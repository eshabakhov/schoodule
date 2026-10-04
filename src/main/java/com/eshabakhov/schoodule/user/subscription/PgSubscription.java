/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user.subscription;

import com.eshabakhov.schoodule.user.Subscription;
import java.time.Instant;
import java.time.OffsetDateTime;
import org.jooq.DSLContext;
import org.jooq.Record1;

/**
 * Immutable subscription snapshot loaded from PostgreSQL.
 * Usage example:
 * <pre>
 * final Subscription sub = new PgSubscription(
 *     SubscriptionPlan.PRO_MAKER,
 *     expiresAt
 * );
 * if (sub.valid()) { ... }
 * </pre>
 *
 * @since 0.0.1
 */
public final class PgSubscription implements Subscription {

    /** Subscription table. */
    private static final com.eshabakhov.schoodule.tables.Subscription SUBSCRIPTION =
        com.eshabakhov.schoodule.tables.Subscription.SUBSCRIPTION;

    /** DSL context. */
    private final DSLContext dsl;

    /** User ID. */
    private final long id;

    /**
     * New subscription.
     *
     * @param dsl Database context
     * @param id User identifier
     */
    public PgSubscription(final DSLContext dsl, final long id) {
        this.dsl = dsl;
        this.id = id;
    }

    @Override
    public Plan plan() {
        final Record1<String> selected = this.dsl.select(PgSubscription.SUBSCRIPTION.PLAN)
            .from(PgSubscription.SUBSCRIPTION)
            .where(PgSubscription.SUBSCRIPTION.USER_ID.eq(this.id))
            .fetchOne();
        final Plan plan;
        if (selected == null) {
            plan = Plan.BASIC;
        } else {
            plan = Plan.valueOf(selected.get(PgSubscription.SUBSCRIPTION.PLAN));
        }
        return plan;
    }

    @Override
    public OffsetDateTime expiry() {
        final Record1<OffsetDateTime> selected = this.dsl.select(
            PgSubscription.SUBSCRIPTION.EXPIRES_AT
        ).from(PgSubscription.SUBSCRIPTION)
            .where(PgSubscription.SUBSCRIPTION.USER_ID.eq(this.id))
            .fetchOne();
        final OffsetDateTime expiry;
        if (selected == null || selected.get(PgSubscription.SUBSCRIPTION.EXPIRES_AT) == null) {
            expiry = OffsetDateTime.MAX;
        } else {
            expiry = selected.get(PgSubscription.SUBSCRIPTION.EXPIRES_AT);
        }
        return expiry;
    }

    @Override
    public boolean valid() {
        return this.expiry().toInstant().isAfter(Instant.now());
    }
}
