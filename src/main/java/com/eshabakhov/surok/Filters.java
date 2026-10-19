/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok;

import java.util.Set;

/**
 * Filter parameters.
 *
 * @since 0.0.1
 */
@FunctionalInterface
public interface Filters {

    /**
     * Filter parameters.
     *
     * @return Filters
     */
    Set<Filter> filters();
}
