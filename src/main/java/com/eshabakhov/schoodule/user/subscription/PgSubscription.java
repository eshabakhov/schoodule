/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user.subscription;

import com.eshabakhov.schoodule.user.Subscription;
import com.eshabakhov.schoodule.user.SubscriptionPlan;
import java.time.Instant;
import java.time.OffsetDateTime;

/**
 * Immutable subscription snapshot loaded from PostgreSQL.
 *
 * <p>Usage example:
 * <pre>
 * final Subscription sub = new PgSubscription(
 *     SubscriptionPlan.PRO_MAKER,
 *     expiresAt
 * );
 * if (sub.valid()) { ... }
 * </pre>
 * </p>
 *
 * @since 0.0.1
 */
public final class PgSubscription implements Subscription {

    /** Current plan. */
    private final SubscriptionPlan current;

    /** Expiry; {@link OffsetDateTime#MAX} for perpetual plans. */
    private final OffsetDateTime exp;

    /**
     * Primary constructor.
     *
     * @param current Plan in effect
     * @param expiry  Expiry timestamp; use {@link OffsetDateTime#MAX} for no expiry
     */
    public PgSubscription(final SubscriptionPlan current, final OffsetDateTime expiry) {
        this.current = current;
        this.exp = expiry;
    }

    @Override
    public SubscriptionPlan plan() {
        return this.current;
    }

    @Override
    public OffsetDateTime expiry() {
        return this.exp;
    }

    @Override
    public boolean valid() {
        return this.exp.toInstant().isAfter(Instant.now());
    }
}
