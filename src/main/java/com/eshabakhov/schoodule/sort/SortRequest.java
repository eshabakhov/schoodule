/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.sort;

import com.eshabakhov.schoodule.Sort;
import lombok.EqualsAndHashCode;

/**
 * Request sorting parameter.
 *
 * @since 0.0.1
 */
@EqualsAndHashCode
public final class SortRequest implements Sort {

    /**
     * Sorting field name.
     */
    private final String field;

    /**
     * Sorting direction.
     */
    private final Direction way;

    /**
     * Ctor.
     *
     * @param field Sorting field name
     * @param way Sorting direction
     * @since 0.0.1
     */
    public SortRequest(final String field, final Direction way) {
        this.field = field;
        this.way = way;
    }

    @Override
    public String name() {
        return this.field;
    }

    @Override
    public Direction direction() {
        return this.way;
    }
}
