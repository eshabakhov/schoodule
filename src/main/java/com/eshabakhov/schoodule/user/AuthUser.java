/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user;

import com.eshabakhov.schoodule.User;
import java.util.Collection;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * Custom UserDetails and OAuth2User principal.
 *
 * <p>Implements both {@link UserDetails} and {@link OAuth2User} so that
 * {@code @AuthenticationPrincipal AuthUser} resolves correctly regardless
 * of whether the user signed in via form-login or Yandex OAuth2.</p>
 *
 * <p>Usage example:
 * <pre>
 * final AuthUser user = new AuthUser(dbUser);
 * user.uid();           // always available
 * user.getUsername();   // from UserDetails
 * user.getName();       // from OAuth2User — same as username
 * </pre>
 * </p>
 *
 * @since 0.0.1
 */
public final class AuthUser implements UserDetails, OAuth2User, User {

    /** User. */
    private final User user;

    public AuthUser(final User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.user.roles()
            .list()
            .stream()
            .map(role -> new SimpleGrantedAuthority(String.format("ROLE_%s", role.name())))
            .toList();
    }

    @Override
    public String getPassword() {
        return this.user.credentials().password();
    }

    @Override
    public String getUsername() {
        return this.user.credentials().username();
    }

    @Override
    public boolean isEnabled() {
        return this.user.info().alive();
    }

    @Override
    public String getName() {
        return this.user.credentials().username();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of();
    }

    @Override
    public long uid() {
        return this.user.uid();
    }

    @Override
    public Credentials credentials() {
        return this.user.credentials();
    }

    @Override
    public Roles roles() {
        return this.user.roles();
    }

    @Override
    public ReferenceInformation info() {
        return this.user.info();
    }

    @Override
    public boolean isAdmin() {
        return this.user.isAdmin();
    }
}
