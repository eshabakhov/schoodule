/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.user;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.User;

/**
 * In-memory user.
 *
 * @since 0.0.1
 */
public final class UrFake implements User {

    /** User identity. */
    private final long identity;

    /** User credentials. */
    private final Credentials credentials;

    /** User roles. */
    private final Roles roles;

    /**
     * New in-memory user.
     *
     * @param identity User identity
     * @param credentials User credentials
     * @param roles User roles
     */
    public UrFake(
        final long identity,
        final Credentials credentials,
        final Roles roles
    ) {
        this.identity = identity;
        this.credentials = credentials;
        this.roles = roles;
    }

    @Override
    public long uid() {
        return this.identity;
    }

    @Override
    public Credentials credentials() {
        return this.credentials;
    }

    @Override
    public Roles roles() {
        return this.roles;
    }

    @Override
    public ReferenceInformation info() {
        return new RiFake(null, false, true);
    }

    @Override
    public boolean isAdmin() {
        return this.roles.list().stream().anyMatch(
            role -> Roles.RoleEnum.ADMIN.name().equals(role.name())
        );
    }

    @Override
    public <T> Media<T> print(final Media<T> media) {
        return media.with("id", this.uid())
            .with("username", this.credentials.username())
            .with("admin", this.isAdmin());
    }
}
