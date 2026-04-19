/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user.credentials;

import com.eshabakhov.schoodule.user.Credentials;

/**
 * Plain implementation of {@link Credentials}.
 *
 * @since 0.0.1
 */
public final class CdBase implements Credentials {

    /** Username. */
    private final String user;

    /** Password. */
    private final String pass;

    public CdBase(final String username, final String password) {
        this.user = username;
        this.pass = password;
    }

    @Override
    public String username() {
        return this.user;
    }

    @Override
    public String password() {
        return this.pass;
    }
}
