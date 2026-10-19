/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok;

/**
 * Pagination parameters for queries.
 *
 * @since 0.0.1
 */
public interface Page {

    /**
     * Maximum number of items per page.
     *
     * @return Page size
     */
    int limit();

    /**
     * Number of items to skip before returning results.
     *
     * @return Offset
     */
    int offset();
}
