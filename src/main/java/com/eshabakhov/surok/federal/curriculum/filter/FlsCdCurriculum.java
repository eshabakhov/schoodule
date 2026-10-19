/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.federal.curriculum.filter;

import com.eshabakhov.surok.Filter;
import com.eshabakhov.surok.Filters;
import com.eshabakhov.surok.filter.FlCdTrue;
import com.eshabakhov.surok.filter.FlConditional;
import com.eshabakhov.surok.filter.FlsConditional;
import java.util.Set;
import org.jooq.Condition;

/**
 * Federal curriculum conditional filters.
 *
 * @since 0.0.1
 */
public final class FlsCdCurriculum implements FlsConditional {

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
    public FlsCdCurriculum(final Filters origin) {
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
            if ("title".equals(item.name()) || "title_ct".equals(item.name())) {
                filter = new FlCdCurriculumByTitle(filter, item.value());
            }
        }
        return filter.condition();
    }
}
