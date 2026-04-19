/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user.role;

import com.eshabakhov.schoodule.user.Role;

/**
 * Simple user role implementation.
 *
 * <p>Immutable object representing user role assignment.</p>
 *
 * @since 0.0.1
 */
public final class RlBase implements Role {

    /** User role ID. */
    private final long identity;

    /** Role name. */
    private final String permission;

    public RlBase(final long id, final String name) {
        this.identity = id;
        this.permission = name;
    }

    @Override
    public long uid() {
        return this.identity;
    }

    @Override
    public String name() {
        return this.permission;
    }
}
