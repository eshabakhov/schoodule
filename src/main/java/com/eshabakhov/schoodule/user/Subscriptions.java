/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user;

/**
 * Collection of personal-user subscriptions.
 *
 * <p>Encapsulates all subscription state transitions and keeps
 * {@code user_role} in sync. Corporate users are rejected immediately.</p>
 *
 * <p>Usage example:
 * <pre>
 * final Subscription sub = subscriptions.subscription(userId);
 * subscriptions.activate(userId, SubscriptionPlan.PRO_MAKER);
 * </pre>
 * </p>
 *
 * @since 0.0.1
 */
public interface Subscriptions {

    /**
     * Returns the current subscription for a personal user.
     * Creates a BASIC_MAKER subscription row if none exists yet.
     *
     * @return Current subscription, never null
     */
    Subscription subscription() throws Exception;

    /**
     * Activates {@code plan} for a personal user.
     * Sets expiry to 30 days from now; BASIC_MAKER has no expiry.
     * Replaces all subscription-managed roles in a single transaction.
     *
     * @param plan Target plan
     */
    void subscription(SubscriptionPlan plan) throws Exception;
}
