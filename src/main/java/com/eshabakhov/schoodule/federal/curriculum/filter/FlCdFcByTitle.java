/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.filter;

import com.eshabakhov.schoodule.filter.FlConditional;
import org.jooq.Condition;

/**
 * Federal curriculum title filter.
 *
 * @since 0.0.1
 */
public final class FlCdFcByTitle implements FlConditional {

    /**
     * JOOQ Table for FederalCurriculum.
     */
    private static final com.eshabakhov.schoodule.tables.FederalCurriculum CURRICULUM =
        com.eshabakhov.schoodule.tables.FederalCurriculum.FEDERAL_CURRICULUM;

    /**
     * Origin filter.
     */
    private final FlConditional origin;

    /**
     * Title to search.
     */
    private final String title;

    public FlCdFcByTitle(final FlConditional origin, final String title) {
        this.origin = origin;
        this.title = title;
    }

    @Override
    public Condition condition() {
        Condition condition = this.origin.condition();
        if (this.title != null && !this.title.isBlank()) {
            condition = condition.and(
                FlCdFcByTitle.CURRICULUM.TITLE.likeIgnoreCase(
                    String.format("%%%s%%", this.title.trim())
                )
            );
        }
        return condition;
    }
}
