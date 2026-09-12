/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.sort;

import com.eshabakhov.schoodule.Sort;
import com.eshabakhov.schoodule.Sorts;
import java.util.List;
import lombok.EqualsAndHashCode;

/**
 * Request sorting parameters.
 *
 * @since 0.0.1
 */
@EqualsAndHashCode
public final class SortsRequest implements Sorts {

    /**
     * Sorting parameters.
     */
    private final List<Sort> origin;

    /**
     * Ctor.
     *
     * @param origin Sorting parameters
     * @since 0.0.1
     */
    public SortsRequest(final List<Sort> origin) {
        this.origin = origin;
    }

    @Override
    public List<Sort> sorts() {
        return this.origin;
    }
}
