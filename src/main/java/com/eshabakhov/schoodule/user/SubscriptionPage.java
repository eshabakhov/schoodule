/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user;

import com.eshabakhov.schoodule.user.subscription.PgSubscriptions;
import java.util.Map;
import org.jooq.DSLContext;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

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
     * @return Template name or redirect
     * @throws Exception if database access fails
     */
    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView page(@AuthenticationPrincipal final AuthUser user) throws Exception {
        final ModelAndView model;
        if (user.info().corporate()) {
            model = new ModelAndView("redirect:/users/profile");
        } else {
            model = new ModelAndView("user/subscription")
                .addAllObjects(
                    Map.of(
                        "pageTitle", "Подписка",
                        "subscription", new PgSubscriptions(this.ctx, user).subscription(),
                        "plans", Subscription.Plan.values()
                    )
                );
        }
        return model;
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
    public ModelAndView checkout(
        @AuthenticationPrincipal
        final AuthUser user,
        @RequestParam
        final String plan
    ) throws Exception {
        final ModelAndView model;
        if (user.info().corporate()) {
            model = new ModelAndView("redirect:/users/profile");
        } else {
            final Subscription.Plan selected = Subscription.Plan.valueOf(plan);
            if (selected == Subscription.Plan.BASIC) {
                model = new ModelAndView("redirect:/users/subscription");
            } else {
                new PgSubscriptions(this.ctx, user).subscription(selected);
                model = new ModelAndView(
                    String.format("redirect:/users/subscription?activated=%s", selected.name())
                );
            }
        }
        return model;
    }

    /**
     * Cancels the current plan and downgrades to BASIC.
     *
     * @param user Authenticated user
     * @return Redirect URL
     * @throws Exception if downgrade fails
     */
    @PostMapping("/cancel")
    public ModelAndView cancel(@AuthenticationPrincipal final AuthUser user) throws Exception {
        final ModelAndView model;
        if (user.info().corporate()) {
            model = new ModelAndView("redirect:/users/profile");
        } else {
            new PgSubscriptions(this.ctx, user).subscription(Subscription.Plan.BASIC);
            model = new ModelAndView("redirect:/users/subscription?cancelled");
        }
        return model;
    }
}
