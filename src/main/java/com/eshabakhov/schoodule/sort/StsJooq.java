/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.sort;

import com.eshabakhov.schoodule.Sorts;
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
     * @return Sort fields
     */
    List<SortField<?>> fields();
}
