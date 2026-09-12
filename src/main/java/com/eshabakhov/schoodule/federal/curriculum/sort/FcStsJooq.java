/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.sort;

import com.eshabakhov.schoodule.Sort;
import com.eshabakhov.schoodule.Sorts;
import com.eshabakhov.schoodule.sort.StJooq;
import com.eshabakhov.schoodule.sort.StsJooq;
import java.util.ArrayList;
import java.util.List;
import org.jooq.SortField;

/**
 * Federal curriculum JOOQ sorting parameters.
 *
 * @since 0.0.1
 */
public final class FcStsJooq implements StsJooq {

    /**
     * Origin sorting parameters.
     */
    private final Sorts origin;

    /**
     * Ctor.
     *
     * @param origin Origin sorting parameters
     * @since 0.0.1
     */
    public FcStsJooq(final Sorts origin) {
        this.origin = origin;
    }

    @Override
    public List<Sort> sorts() {
        return this.origin.sorts();
    }

    @Override
    public List<SortField<?>> fields() {
        final List<SortField<?>> fields = new ArrayList<>(this.origin.sorts().size());
        for (final Sort sort : this.origin.sorts()) {
            final StJooq field = new FcStJooq(sort);
            if (field.field() != null) {
                fields.add(field.field());
            }
        }
        return fields;
    }
}
