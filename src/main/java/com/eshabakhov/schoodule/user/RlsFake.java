/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory user roles.
 *
 * @since 0.0.1
 */
public final class RlsFake implements Roles {

    /** Role assignments. */
    private final List<Role> roles;

    /** Role assignment identity sequence. */
    private final AtomicLong identities;

    /**
     * New empty in-memory roles.
     */
    public RlsFake() {
        this(new CopyOnWriteArrayList<>(), new AtomicLong(0L));
    }

    /**
     * New in-memory roles.
     *
     * @param roles Role assignments
     * @param identities Role assignment identity sequence
     */
    public RlsFake(final List<Role> roles, final AtomicLong identities) {
        this.roles = roles;
        this.identities = identities;
    }

    @Override
    public Role grant(final RoleEnum role) {
        final Role granted = new RlFake(
            this.identities.incrementAndGet(), role.name()
        );
        this.roles.add(granted);
        return granted;
    }

    @Override
    public List<Role> list() {
        return List.copyOf(this.roles);
    }

    @Override
    public void revoke(final long identity) {
        this.roles.removeIf(role -> role.uid() == identity);
    }
}
