/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.media;

import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.Schoodule;
import com.eshabakhov.schoodule.Schools;
import com.eshabakhov.schoodule.User;
import com.eshabakhov.schoodule.security.Csrf;

/**
 * Web representation of Schoodule shared by browser pages.
 *
 * @since 0.0.1
 */
public final class ScWeb implements Schoodule {

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
    public ScWeb(final User user, final Schools schools, final Csrf csrf) {
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
