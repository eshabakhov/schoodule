/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.filter;

import com.eshabakhov.surok.Filter;
import org.jooq.Condition;

/**
 * Filter that can be transformed to a JOOQ condition.
 *
 * @since 0.0.1
 */
public interface FlConditional extends Filter {

    @Override
    default String name() {
        return "";
    }

    @Override
    default String value() {
        return "";
    }

    /**
     * Builds condition for filtering.
     *
     * @return JOOQ condition
     */
    Condition condition();
}
