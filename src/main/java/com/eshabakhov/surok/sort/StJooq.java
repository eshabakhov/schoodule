/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.sort;

import com.eshabakhov.surok.Sort;
import org.jooq.SortField;

/**
 * JOOQ sorting parameter.
 *
 * @since 0.0.1
 */
public interface StJooq extends Sort {

    /**
     * JOOQ sort field.
     *
     * @return Sort field
     */
    SortField<?> field();
}
