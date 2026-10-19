/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user;

/**
 * In-memory user credentials.
 *
 * @since 0.0.1
 */
public final class CdFake implements Credentials {

    /** Username. */
    private final String login;

    /** Password. */
    private final String secret;

    /**
     * New in-memory credentials.
     *
     * @param login Username
     * @param secret Password
     */
    public CdFake(final String login, final String secret) {
        this.login = login;
        this.secret = secret;
    }

    @Override
    public String username() {
        return this.login;
    }

    @Override
    public String password() {
        return this.secret;
    }
}
