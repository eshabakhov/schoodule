/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.sort;

import com.eshabakhov.surok.Sorts;
import java.util.List;
import org.jooq.SortField;

/**
 * JOOQ sorting parameters.
 *
 * @since 0.0.1
 */
public interface StsJooq extends Sorts {

    /**
     * JOOQ sort fields.
     *
     * @return Sort fields
     */
    List<SortField<?>> fields();
}
