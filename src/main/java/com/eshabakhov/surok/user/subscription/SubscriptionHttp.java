/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.user.subscription;

import com.eshabakhov.surok.media.AcHttp;
import com.eshabakhov.surok.media.MdXml;
import com.eshabakhov.surok.media.MdXslt;
import com.eshabakhov.surok.media.SrWeb;
import com.eshabakhov.surok.media.XsClasspath;
import com.eshabakhov.surok.school.SlsBrief;
import com.eshabakhov.surok.school.SlsPostgres;
import com.eshabakhov.surok.security.CsToken;
import com.eshabakhov.surok.user.AuthUser;
import com.eshabakhov.surok.user.Subscription;
import com.eshabakhov.surok.user.UrBrief;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.jooq.DSLContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Personal subscription pages and CSRF-protected activation and cancellation forms.
 * Corporate accounts and administrators are redirected to their profile.
 *
 * @since 0.0.1
 */
@RestController
@RequestMapping("/users/subscription")
public final class SubscriptionHttp {

    /** Database context. */
    private final DSLContext ctx;

    /**
     * New subscription resource.
     *
     * @param ctx Database context
     */
    public SubscriptionHttp(final DSLContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Render the personal subscription page.
     *
     * @param user Current user
     * @param request HTTP request
     * @return Subscription page or profile redirect
     * @throws Exception If printing or lookup fails
     */
    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> page(
        @AuthenticationPrincipal final AuthUser user,
        final HttpServletRequest request
    ) throws Exception {
        final MediaType type = new AcHttp(request).type();
        final ResponseEntity<String> response;
        if (user.info().corporate() || user.isAdmin()) {
            response = ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/users/profile")).build();
        } else {
            response = ResponseEntity.ok()
                .contentType(new MediaType(type, StandardCharsets.UTF_8)).body(
                    new SrWeb(
                        new UrBrief(user), new SlsBrief(new SlsPostgres(this.ctx)),
                        new CsToken((CsrfToken) request.getAttribute(CsrfToken.class.getName()))
                    ).print(
                        new MdXslt(
                            new MdXml("surok").with("path", request.getRequestURI()),
                            new XsClasspath("xslt/subscription.xsl", "subscription")
                        )
                    ).with(
                        "subscription",
                        new SpWeb(new PgSubscriptions(this.ctx, user).subscription())
                    ).with("activated", request.getParameterMap().containsKey("activated"))
                        .with("cancelled", request.getParameterMap().containsKey("cancelled"))
                        .with("error", request.getParameterMap().containsKey("error")).print()
                );
        }
        return response;
    }

    /**
     * Activate a non-basic plan for thirty days in the existing test mode.
     *
     * @param user Current user
     * @param plan Selected plan
     * @param request HTTP request
     * @return Subscription or profile redirect
     * @throws Exception If activation fails
     */
    @PostMapping(
        value = "/checkout", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = MediaType.TEXT_HTML_VALUE
    )
    public ResponseEntity<Void> checkout(
        @AuthenticationPrincipal final AuthUser user,
        @RequestParam final String plan,
        final HttpServletRequest request
    ) throws Exception {
        new AcHttp(request).type();
        String target = "/users/profile";
        if (!user.info().corporate() && !user.isAdmin()) {
            final Subscription.Plan selected;
            try {
                selected = Subscription.Plan.valueOf(plan);
            } catch (final IllegalArgumentException exception) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Unknown subscription plan", exception
                );
            }
            target = "/users/subscription";
            if (selected != Subscription.Plan.BASIC) {
                new PgSubscriptions(this.ctx, user).subscription(selected);
                target = String.format("/users/subscription?activated=%s", selected.name());
            }
        }
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(target)).build();
    }

    /**
     * Cancel the current plan and restore the basic subscription roles.
     *
     * @param user Current user
     * @param request HTTP request
     * @return Subscription or profile redirect
     * @throws Exception If cancellation fails
     */
    @PostMapping(
        value = "/cancel", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = MediaType.TEXT_HTML_VALUE
    )
    public ResponseEntity<Void> cancel(
        @AuthenticationPrincipal final AuthUser user,
        final HttpServletRequest request
    ) throws Exception {
        new AcHttp(request).type();
        String target = "/users/profile";
        if (!user.info().corporate() && !user.isAdmin()) {
            new PgSubscriptions(this.ctx, user).subscription(Subscription.Plan.BASIC);
            target = "/users/subscription?cancelled";
        }
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(target)).build();
    }
}
