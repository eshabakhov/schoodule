/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.user.subscription;

import com.eshabakhov.surok.sql.ClasspathSql;
import com.eshabakhov.surok.user.Subscription;
import java.time.Instant;
import java.time.OffsetDateTime;
import org.jooq.DSLContext;
import org.jooq.Record;

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
        final Record selected = this.dsl.resultQuery(
            new ClasspathSql("subscription/find-plan.sql").text(),
            this.id
        ).fetchOne();
        final Plan plan;
        if (selected == null) {
            plan = Plan.BASIC;
        } else {
            plan = Plan.valueOf(selected.get("plan", String.class));
        }
        return plan;
    }

    @Override
    public OffsetDateTime expiry() {
        final Record selected = this.dsl.resultQuery(
            new ClasspathSql("subscription/find-expiry.sql").text(),
            this.id
        ).fetchOne();
        final OffsetDateTime expiry;
        if (selected == null || selected.get("expires_at", OffsetDateTime.class) == null) {
            expiry = OffsetDateTime.MAX;
        } else {
            expiry = selected.get("expires_at", OffsetDateTime.class);
        }
        return expiry;
    }

    @Override
    public boolean valid() {
        return this.expiry().toInstant().isAfter(Instant.now());
    }
}
