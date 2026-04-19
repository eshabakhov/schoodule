/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.security;

import com.eshabakhov.schoodule.user.AuthUser;
import com.eshabakhov.schoodule.user.UrsPostgres;
import org.jooq.DSLContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Database user details service for Spring Security.
 *
 * <p>Loads user details from PostgreSQL database.</p>
 *
 * @since 0.0.1
 */
@Service
@SuppressWarnings("PMD.AvoidCatchingGenericException")
public final class DatabaseUserDetailsService implements UserDetailsService {

    /** Database context. */
    private final DSLContext ctx;

    DatabaseUserDetailsService(final DSLContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        try {
            return new AuthUser(new UrsPostgres(this.ctx).authentication(username));
            //@checkstyle IllegalCatch (1 line)
        } catch (final Exception ex) {
            throw new UsernameNotFoundException(String.format("User '%s' not found", username), ex);
        }
    }
}
