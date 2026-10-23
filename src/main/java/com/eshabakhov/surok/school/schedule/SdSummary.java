/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.schedule;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.Printable;
import com.eshabakhov.surok.sql.ClasspathSql;
import org.jooq.DSLContext;

/**
 * School class and assigned subject counts for a schedule's details page.
 *
 * @since 0.0.1
 */
public final class SdSummary implements Printable {

    /** Database context. */
    private final DSLContext ctx;

    /** School identifier. */
    private final Long school;

    /** Schedule identifier. */
    private final Long schedule;

    /**
     * New schedule summary.
     *
     * @param ctx Database context
     * @param school School identifier
     * @param schedule Schedule identifier
     */
    public SdSummary(final DSLContext ctx, final Long school, final Long schedule) {
        this.ctx = ctx;
        this.school = school;
        this.schedule = schedule;
    }

    @Override
    public <T> Media<T> print(final Media<T> media) {
        return media.with(
            "classCount",
            this.ctx.resultQuery(
                new ClasspathSql("schedule/count-classes.sql").text(), this.school, false
            ).fetchSingle(0, Integer.class)
        ).with(
            "subjectCount",
            this.ctx.resultQuery(
                new ClasspathSql("schedule/count-subjects.sql").text(), this.schedule
            ).fetchSingle(0, Integer.class)
        );
    }
}
