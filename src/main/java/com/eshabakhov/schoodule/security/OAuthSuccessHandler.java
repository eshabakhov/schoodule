/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.security;

import com.eshabakhov.schoodule.user.AuthUser;
import com.eshabakhov.schoodule.user.RoleEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * OAuth2 login success handler for Yandex.
 *
 * <p>At this point {@link YandexOAuth2UserService} has already resolved the
 * principal to an {@link AuthUser}, so this handler applies the same redirect
 * rule as {@link LoginSuccessHandler}: ADMIN goes to /schools,
 * everyone else goes to /schools/{schoolId}.</p>
 *
 * <p>Usage example:
 * <pre>
 * // Wired automatically by Spring Security via SecurityConfig.
 * </pre>
 * </p>
 *
 * @since 0.0.1
 */
@Component
public final class OAuthSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final Authentication authentication
    ) throws IOException {
        final AuthUser user = (AuthUser) authentication.getPrincipal();
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
