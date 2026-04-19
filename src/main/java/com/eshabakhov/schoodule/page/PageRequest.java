/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.page;

import com.eshabakhov.schoodule.Page;
import lombok.EqualsAndHashCode;

/**
 * Implementation of {@link Page} that parses pagination parameters from a JSON string.
 *
 * @since 0.0.1
 */
@EqualsAndHashCode
public final class PageRequest implements Page {

    /** Page's limit. */
    private final int lim;

    /** Page's offset. */
    private final int off;

    public PageRequest(final int lim, final int off) {
        this.lim = lim;
        this.off = off;
    }

    @Override
    public int limit() {
        return this.lim;
    }

    @Override
    public int offset() {
        return this.off;
    }
}
