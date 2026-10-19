/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.filter;

import com.eshabakhov.surok.Filter;
import com.eshabakhov.surok.Filters;
import java.util.Set;
import lombok.EqualsAndHashCode;

/**
 * Request filter parameters.
 *
 * @since 0.0.1
 */
@EqualsAndHashCode
public final class FiltersRequest implements Filters {

    /**
     * Filter parameters.
     */
    private final Set<Filter> origin;

    /**
     * Ctor.
     *
     * @param origin Filter parameters
     * @since 0.0.1
     */
    public FiltersRequest(final Set<Filter> origin) {
        this.origin = origin;
    }

    @Override
    public Set<Filter> filters() {
        return this.origin;
    }
}
