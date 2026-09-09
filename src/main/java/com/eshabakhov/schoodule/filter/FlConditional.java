/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.filter;

import com.eshabakhov.schoodule.Filter;
import org.jooq.Condition;

/**
 * Filter that can be transformed to a JOOQ condition.
 *
 * @since 0.0.1
 */
public interface FlConditional extends Filter {

    /**
     * Builds condition for filtering.
     *
     * @return JOOQ condition
     */
    Condition condition();
}
