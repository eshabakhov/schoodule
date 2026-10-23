/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.schedule.load;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.PrItems;
import com.eshabakhov.surok.Printable;
import com.eshabakhov.surok.sql.ClasspathSql;
import org.jooq.DSLContext;

/**
 * Active subject options restricted to one school.
 *
 * @since 0.0.1
 */
public final class ClSubjects implements Printable {

    /** Database context. */
    private final DSLContext ctx;

    /** School identifier. */
    private final long school;

    /**
     * New subject options.
     *
     * @param ctx Database context
     * @param school School identifier
     */
    public ClSubjects(final DSLContext ctx, final long school) {
        this.ctx = ctx;
        this.school = school;
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return media.with(
            new PrItems(
                "subject", this.ctx.resultQuery(
                    new ClasspathSql("class-load/subjects.sql").text(), this.school
                ).fetch(
                    row -> new ClSubject(row.get("id", Long.class), row.get("name", String.class))
                )
            )
        );
    }
}
