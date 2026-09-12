/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.filter;

import com.eshabakhov.schoodule.Filter;
import lombok.EqualsAndHashCode;

/**
 * Request filter parameter.
 *
 * @since 0.0.1
 */
@EqualsAndHashCode
public final class FilterRequest implements Filter {

    /**
     * Filter field name.
     */
    private final String field;

    /**
     * Filter value.
     */
    private final String val;

    /**
     * Ctor.
     *
     * @param field Filter field name
     * @param val Filter value
     * @since 0.0.1
     */
    public FilterRequest(final String field, final String val) {
        this.field = field;
        this.val = val;
    }

    @Override
    public String name() {
        return this.field;
    }

    @Override
    public String value() {
        return this.val;
    }
}
