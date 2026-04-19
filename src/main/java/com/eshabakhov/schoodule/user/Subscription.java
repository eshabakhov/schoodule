/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user;

import java.time.OffsetDateTime;

/**
 * A personal user's subscription to Schoodule.
 *
 * <p>Represents one subscription row and exposes behavioral methods
 * rather than getters. Corporate users never have a subscription.</p>
 *
 * <p>Usage example:
 * <pre>
 * final Subscription sub = subscriptions.subscription(userId);
 * if (sub.expired()) {
 *     subscriptions.downgrade(userId);
 * }
 * </pre>
 * </p>
 *
 * @since 0.0.1
 */
public interface Subscription {

    /**
     * The subscription plan currently in effect.
     *
     * @return Current plan
     */
    SubscriptionPlan plan();

    /**
     * The date and time when the subscription expires.
     * Returns {@link OffsetDateTime#MAX} for perpetual (BASIC_MAKER) plans.
     *
     * @return Expiry instant, never null
     */
    OffsetDateTime expiry();

    /**
     * Whether this subscription is currently valid (not yet expired).
     *
     * @return True if active
     */
    boolean valid();
}
