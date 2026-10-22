/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.building.filter;

import com.eshabakhov.surok.filter.FlConditional;
import com.eshabakhov.surok.sql.ClasspathSql;
import org.jooq.Condition;
import org.jooq.impl.DSL;

/**
 * School building name filter.
 *
 * @since 0.0.1
 */
public final class FlCdBuildingByName implements FlConditional {

    /**
     * Origin filter.
     */
    private final FlConditional origin;

    /**
     * Name to search.
     */
    private final String name;

    /**
     * New name filter.
     *
     * @param origin Origin filter
     * @param name Name to search
     */
    public FlCdBuildingByName(final FlConditional origin, final String name) {
        this.origin = origin;
        this.name = name;
    }

    @Override
    public Condition condition() {
        Condition condition = this.origin.condition();
        if (this.name != null && !this.name.isBlank()) {
            condition = condition.and(
                DSL.condition(
                    new ClasspathSql("building/conditions/by-name.sql").text(),
                    String.format("%%%s%%", this.name.trim())
                )
            );
        }
        return condition;
    }
}
