/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user;

import com.eshabakhov.schoodule.user.subscription.PgSubscriptions;
import org.jooq.DSLContext;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * HTTP endpoints for subscription management.
 *
 * <p>Accessible only to personal (non-corporate) users.
 * Corporate users are redirected to their profile page immediately.</p>
 *
 * <p>Usage example:
 * <pre>
 * GET  /users/subscription          → manage page
 * POST /users/subscription/checkout → activate a paid plan (stub)
 * POST /users/subscription/cancel   → downgrade to BASIC
 * </pre>
 * </p>
 *
 * @since 0.0.1
 */
@Controller
@RequestMapping("/users/subscription")
public final class SubscriptionPage {

    /** Database context. */
    private final DSLContext ctx;

    public SubscriptionPage(final DSLContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Renders the subscription management page for personal users.
     *
     * @param user  Authenticated user
     * @param model View model
     * @return Template name or redirect
     * @throws Exception if database access fails
     */
    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public String page(
        @AuthenticationPrincipal
        final AuthUser user,
        final Model model
    ) throws Exception {
        final String page;
        if (user.info().corporate()) {
            page = "redirect:/users/profile";
        } else {
            model.addAttribute("pageTitle", "Подписка");
            model.addAttribute(
                "subscription",
                new PgSubscriptions(this.ctx, user).subscription()
            );
            model.addAttribute("plans", SubscriptionPlan.values());
            page = "user/subscription";
        }
        return page;
    }

    /**
     * Activates the chosen plan for the current user (stub, no real payment).
     *
     * @param user Authenticated user
     * @param plan Plan name from the form
     * @return Redirect URL
     * @throws Exception if activation fails
     */
    @PostMapping("/checkout")
    public String checkout(
        @AuthenticationPrincipal
        final AuthUser user,
        @RequestParam
        final String plan
    ) throws Exception {
        final String page;
        if (user.info().corporate()) {
            page = "redirect:/users/profile";
        } else {
            final SubscriptionPlan selected = SubscriptionPlan.valueOf(plan);
            if (selected == SubscriptionPlan.BASIC) {
                page = "redirect:/users/subscription";
            } else {
                new PgSubscriptions(this.ctx, user).subscription(selected);
                page = String.format("redirect:/users/subscription?activated=%s", selected.name());
            }
        }
        return page;
    }

    /**
     * Cancels the current plan and downgrades to BASIC.
     *
     * @param user Authenticated user
     * @return Redirect URL
     * @throws Exception if downgrade fails
     */
    @PostMapping("/cancel")
    public String cancel(@AuthenticationPrincipal final AuthUser user) throws Exception {
        final String page;
        if (user.info().corporate()) {
            page = "redirect:/users/profile";
        } else {
            new PgSubscriptions(this.ctx, user).subscription(SubscriptionPlan.BASIC);
            page = "redirect:/users/subscription?cancelled";
        }
        return page;
    }
}
