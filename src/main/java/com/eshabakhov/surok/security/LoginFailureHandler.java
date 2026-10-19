/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.security;

import com.eshabakhov.surok.User;
import com.eshabakhov.surok.tables.LoginAttempt;
import com.eshabakhov.surok.user.UrsPostgres;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import org.jooq.DSLContext;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

/**
 * Handler for login failures.
 *
 * @since 0.0.1
 */
@Component
@SuppressWarnings("PMD.AvoidCatchingGenericException")
public final class LoginFailureHandler implements AuthenticationFailureHandler {

    /** Database context. */
    private final DSLContext ctx;

    LoginFailureHandler(final DSLContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void onAuthenticationFailure(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final AuthenticationException exception
    ) throws IOException {
        final String error = Map.of(
            BadCredentialsException.class, "bad_credentials",
            UsernameNotFoundException.class, "user_not_found"
        ).getOrDefault(exception.getClass(), "unknown");
        if ("bad_credentials".equals(error)) {
            final User user;
            try {
                user = new UrsPostgres(this.ctx)
                    .identification(exception.getAuthenticationRequest().getPrincipal().toString());
            // @checkstyle IllegalCatchCheck (1 line)
            } catch (final Exception ex) {
                throw new IOException(ex);
            }
            this.ctx.insertInto(LoginAttempt.LOGIN_ATTEMPT)
                .set(LoginAttempt.LOGIN_ATTEMPT.USER_ID, user.uid())
                .set(LoginAttempt.LOGIN_ATTEMPT.TIME, Instant.now().atOffset(ZoneOffset.UTC))
                .set(LoginAttempt.LOGIN_ATTEMPT.SUCCESS, false)
                .execute();
        }
        response.sendRedirect(String.format("/users/login?error=%s", error));
    }
}
