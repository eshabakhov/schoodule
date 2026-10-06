/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.filter;

import com.eshabakhov.schoodule.filter.FlConditional;
import com.eshabakhov.schoodule.tables.FederalCurriculum;
import org.jooq.Condition;

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
                FederalCurriculum.FEDERAL_CURRICULUM.TITLE.likeIgnoreCase(
                    String.format("%%%s%%", this.title.trim())
                )
            );
        }
        return condition;
    }
}
