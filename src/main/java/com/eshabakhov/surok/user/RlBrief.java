/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.user;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.Printable;

/**
 * A role name printed into browser representations.
 *
 * @since 0.0.1
 */
public final class RlBrief implements Printable {

    /** Assigned role. */
    private final Role origin;

    /**
     * New role representation.
     *
     * @param origin Assigned role
     */
    public RlBrief(final Role origin) {
        this.origin = origin;
    }

    @Override
    public <T> Media<T> print(final Media<T> media) {
        return media.with("name", this.origin.name());
    }
}
