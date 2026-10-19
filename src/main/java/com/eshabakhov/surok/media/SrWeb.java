/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.media;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.Schools;
import com.eshabakhov.surok.Surok;
import com.eshabakhov.surok.User;
import com.eshabakhov.surok.security.Csrf;

/**
 * Web representation of Surok shared by browser pages.
 *
 * @since 0.0.1
 */
public final class SrWeb implements Surok {

    /** Current user. */
    private final User user;

    /** Schools collection. */
    private final Schools schools;

    /** CSRF protection. */
    private final Csrf csrf;

    /**
     * New browser representation.
     *
     * @param user Current user
     * @param schools Schools collection
     * @param csrf CSRF protection
     */
    public SrWeb(final User user, final Schools schools, final Csrf csrf) {
        this.user = user;
        this.schools = schools;
        this.csrf = csrf;
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return media
            .with("user", this.user)
            .with("schools", this.schools)
            .with("csrf", this.csrf);
    }
}
