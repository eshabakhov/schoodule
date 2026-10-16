/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.sort;

import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.Printable;
import com.eshabakhov.schoodule.Sort;

/**
 * Sort representation for browser documents.
 *
 * @since 0.0.1
 */
public final class StWeb implements Sort, Printable {

    /** Origin sort. */
    private final Sort origin;

    /**
     * New sort representation.
     *
     * @param origin Origin sort
     */
    public StWeb(final Sort origin) {
        this.origin = origin;
    }

    @Override
    public String name() {
        return this.origin.name();
    }

    @Override
    public Direction direction() {
        return this.origin.direction();
    }

    @Override
    public <T> Media<T> print(final Media<T> media) {
        return media
            .with("name", this.name())
            .with("direction", this.direction().name());
    }
}
