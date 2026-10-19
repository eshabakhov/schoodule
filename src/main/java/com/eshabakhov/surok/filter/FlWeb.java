/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.filter;

import com.eshabakhov.surok.Filter;
import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.Printable;

/**
 * Filter representation for browser documents.
 *
 * @since 0.0.1
 */
public final class FlWeb implements Filter, Printable {

    /** Origin filter. */
    private final Filter origin;

    /**
     * New filter representation.
     *
     * @param origin Origin filter
     */
    public FlWeb(final Filter origin) {
        this.origin = origin;
    }

    @Override
    public String name() {
        return this.origin.name();
    }

    @Override
    public String value() {
        return this.origin.value();
    }

    @Override
    public <T> Media<T> print(final Media<T> media) {
        return media
            .with("name", this.name())
            .with("value", this.value());
    }
}
