/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.filter;

import com.eshabakhov.schoodule.Filter;
import com.eshabakhov.schoodule.Filters;
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
