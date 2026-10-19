/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.security;

import com.eshabakhov.surok.Media;
import org.springframework.security.web.csrf.CsrfToken;

/**
 * Spring Security CSRF token representation.
 *
 * @since 0.0.1
 */
public final class CsToken implements Csrf {

    /** Spring Security token. */
    private final CsrfToken token;

    /**
     * New token representation.
     *
     * @param token Spring Security token
     */
    public CsToken(final CsrfToken token) {
        this.token = token;
    }

    @Override
    public <T> Media<T> print(final Media<T> media) {
        Media<T> printed = media;
        if (this.token != null) {
            printed = printed
                .with("token", this.token.getToken())
                .with("header", this.token.getHeaderName())
                .with("parameter", this.token.getParameterName());
        }
        return printed;
    }
}
