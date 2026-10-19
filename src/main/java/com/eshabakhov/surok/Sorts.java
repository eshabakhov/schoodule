/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok;

import java.util.List;

/**
 * Sorting parameters.
 *
 * @since 0.0.1
 */
@FunctionalInterface
public interface Sorts {

    /**
     * Sorting parameters.
     *
     * @return Sorts
     */
    List<Sort> sorts();
}
