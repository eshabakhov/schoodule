/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.sort;

import com.eshabakhov.schoodule.Sort;
import org.jooq.SortField;

/**
 * JOOQ sorting parameter.
 *
 * @since 0.0.1
 */
public interface StJooq extends Sort {

    /**
     * JOOQ sort field.
     * @return Sort field
     */
    SortField<?> field();
}
