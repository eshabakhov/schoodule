/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.user;

import com.eshabakhov.surok.media.AcHttp;
import com.eshabakhov.surok.media.MdXml;
import com.eshabakhov.surok.media.MdXslt;
import com.eshabakhov.surok.media.XsClasspath;
import com.eshabakhov.surok.security.CsToken;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.jooq.DSLContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public authentication pages and protected registration form submission.
 * Login itself is processed by Spring Security at {@code /login}.
 *
 * @since 0.0.1
 */
@RestController
@RequestMapping("/users")
public final class AuthHttp {

    /** Database context. */
    private final DSLContext ctx;

    /**
     * New authentication resource.
     *
     * @param ctx Database context
     */
    public AuthHttp(final DSLContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Render a login or registration form.
     *
     * @param request HTTP request
     * @return Public form
     * @throws Exception If printing fails
     * @checkstyle NonStaticMethodCheck (2 lines)
     */
    @GetMapping(value = {"/login", "/register"}, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> page(final HttpServletRequest request) throws Exception {
        final String view;
        if (request.getRequestURI().endsWith("/register")) {
            view = "register";
        } else {
            view = "login";
        }
        return ResponseEntity.ok().contentType(
            new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8)
        ).body(AuthHttp.page(request, view).print());
    }

    /**
     * Register a personal account from a CSRF-protected browser form.
     *
     * @param parameters Submitted fields
     * @param request HTTP request
     * @return Login redirect or the form containing a validation error
     * @throws Exception If storage or printing fails
     */
    @PostMapping(
        value = "/register",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = MediaType.TEXT_HTML_VALUE
    )
    public ResponseEntity<String> register(
        @RequestParam final MultiValueMap<String, String> parameters,
        final HttpServletRequest request
    ) throws Exception {
        final MdXslt page = AuthHttp.page(request, "register");
        ResponseEntity<String> response;
        try {
            new UrSignup(this.ctx, parameters).register();
            response = ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/users/login?registered")).build();
        } catch (final RegistrationException | UserCreationException exception) {
            response = ResponseEntity.badRequest().contentType(
                new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8)
            ).body(
                page.with("message", exception.getMessage())
                    .with("username", parameters.getFirst("username"))
                    .with("email", parameters.getFirst("email")).print()
            );
        }
        return response;
    }

    private static MdXslt page(final HttpServletRequest request, final String view)
        throws Exception {
        new AcHttp(request).type();
        return new MdXslt(
            new MdXml("surok"), new XsClasspath("xslt/authentication.xsl", view)
        ).with(
            "csrf", new CsToken((CsrfToken) request.getAttribute(CsrfToken.class.getName()))
        ).with("error", request.getParameter("error"))
            .with("logout", request.getParameterMap().containsKey("logout"))
            .with("registered", request.getParameterMap().containsKey("registered"));
    }
}
