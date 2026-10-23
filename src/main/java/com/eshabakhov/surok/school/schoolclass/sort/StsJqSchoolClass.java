/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.schoolclass.sort;

import com.eshabakhov.surok.Sort;
import com.eshabakhov.surok.Sorts;
import com.eshabakhov.surok.sort.SortRequest;
import com.eshabakhov.surok.sort.StsJooq;
import java.util.ArrayList;
import java.util.List;
import org.jooq.SortField;
import org.jooq.impl.DSL;

/**
 * School class JOOQ sorting parameters.
 *
 * @since 0.0.1
 */
public final class StsJqSchoolClass implements StsJooq {

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
    public StsJqSchoolClass(final Sorts origin) {
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
            final SortField<?> field = new StJqSchoolClass(sort).field();
            if (field != null) {
                fields.add(field);
            }
        }
        if (fields.isEmpty()) {
            fields.add(
                new StJqSchoolClass(
                    new SortRequest("name", Sort.Direction.ASC)
                ).field()
            );
        }
        fields.add(DSL.field(DSL.name("public", "school_class", "id"), Long.class).asc());
        return fields;
    }
}
