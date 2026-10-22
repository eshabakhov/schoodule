/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.federal.curriculum.filter;

import com.eshabakhov.surok.filter.FlConditional;
import com.eshabakhov.surok.sql.ClasspathSql;
import org.jooq.Condition;
import org.jooq.impl.DSL;

/**
 * Federal curriculum title filter.
 *
 * @since 0.0.1
 */
public final class FlCdCurriculumByTitle implements FlConditional {

    /**
     * Origin filter.
     */
    private final FlConditional origin;

    /**
     * Title to search.
     */
    private final String title;

    /**
     * New title filter.
     *
     * @param origin Origin filter
     * @param title Title to search
     */
    public FlCdCurriculumByTitle(final FlConditional origin, final String title) {
        this.origin = origin;
        this.title = title;
    }

    @Override
    public Condition condition() {
        Condition condition = this.origin.condition();
        if (this.title != null && !this.title.isBlank()) {
            condition = condition.and(
                DSL.condition(
                    new ClasspathSql("curriculum/conditions/by-title.sql").text(),
                    String.format("%%%s%%", this.title.trim())
                )
            );
        }
        return condition;
    }
}
