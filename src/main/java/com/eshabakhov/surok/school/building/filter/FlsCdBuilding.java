/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.building.filter;

import com.eshabakhov.surok.Filter;
import com.eshabakhov.surok.Filters;
import com.eshabakhov.surok.filter.FlCdTrue;
import com.eshabakhov.surok.filter.FlConditional;
import com.eshabakhov.surok.filter.FlsConditional;
import java.util.Set;
import org.jooq.Condition;

/**
 * School building conditional filters.
 *
 * @since 0.0.1
 */
public final class FlsCdBuilding implements FlsConditional {

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
    public FlsCdBuilding(final Filters origin) {
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
            if ("name".equals(item.name()) || "name_ct".equals(item.name())) {
                filter = new FlCdBuildingByName(filter, item.value());
            }
        }
        return filter.condition();
    }
}
