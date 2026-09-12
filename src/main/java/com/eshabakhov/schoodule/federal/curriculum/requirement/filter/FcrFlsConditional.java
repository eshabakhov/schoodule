/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement.filter;

import com.eshabakhov.schoodule.Filter;
import com.eshabakhov.schoodule.Filters;
import com.eshabakhov.schoodule.filter.FlCdTrue;
import com.eshabakhov.schoodule.filter.FlConditional;
import com.eshabakhov.schoodule.filter.FlsConditional;
import java.util.Set;
import org.jooq.Condition;

/**
 * Federal curriculum requirement conditional filters.
 *
 * @since 0.0.1
 */
public final class FcrFlsConditional implements FlsConditional {

    /**
     * Origin filters.
     */
    private final Filters origin;

    /**
     * Ctor.
     *
     * @param origin Origin filters
     * @since 0.0.1
     */
    public FcrFlsConditional(final Filters origin) {
        this.origin = origin;
    }

    @Override
    public Set<Filter> filters() {
        return this.origin.filters();
    }

    @Override
    public Condition condition() {
        FlConditional filter = new FlCdTrue();
        for (final Filter item : this.origin.filters()) {
            if ("grade".equals(item.name())) {
                filter = new FlCdFcrByGrade(filter, item.value());
            } else if ("subject".equals(item.name()) || "subjectName".equals(item.name())) {
                filter = new FlCdFcrBySubject(filter, item.value());
            } else if ("part".equals(item.name()) || "partType".equals(item.name())) {
                filter = new FlCdFcrByPart(filter, item.value());
            }
        }
        return filter.condition();
    }
}
