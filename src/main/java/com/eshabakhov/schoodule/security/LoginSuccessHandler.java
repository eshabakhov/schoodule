/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.security;

import com.eshabakhov.schoodule.User;
import com.eshabakhov.schoodule.tables.LoginAttempt;
import com.eshabakhov.schoodule.user.RoleEnum;
import com.eshabakhov.schoodule.user.UrsPostgres;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import org.jooq.DSLContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * Handler for success login.
 *
 * @since 0.0.1
 */
@Component
@SuppressWarnings("PMD.AvoidCatchingGenericException")
public final class LoginSuccessHandler implements AuthenticationSuccessHandler {

    /** Database context. */
    private final DSLContext ctx;

    LoginSuccessHandler(final DSLContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void onAuthenticationSuccess(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final Authentication authentication
    ) throws IOException {
        final User user;
        try {
            user = new UrsPostgres(this.ctx).identification(authentication.getName());
        // @checkstyle IllegalCatchCheck (1 line)
        } catch (final Exception ex) {
            throw new IOException(ex);
        }
        this.ctx.insertInto(LoginAttempt.LOGIN_ATTEMPT)
            .set(LoginAttempt.LOGIN_ATTEMPT.USER_ID, user.uid())
            .set(LoginAttempt.LOGIN_ATTEMPT.TIME, Instant.now().atOffset(ZoneOffset.UTC))
            .set(LoginAttempt.LOGIN_ATTEMPT.SUCCESS, true)
            .execute();
        if (user.roles()
            .list()
            .stream()
            .anyMatch(role -> RoleEnum.ADMIN.name().equalsIgnoreCase(role.name()))) {
            response.sendRedirect("/schools");
        } else {
            response.sendRedirect(String.format("/schools/%s", user.info().school()));
        }
    }
}
