/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.schedule.load.input;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.school.schedule.load.ClassLoadInput;
import com.eshabakhov.surok.sql.ClasspathSql;
import java.util.Map;
import org.jooq.DSLContext;

/**
 * Load input referencing active classes and subjects in its schedule's school.
 *
 * @since 0.0.1
 */
public final class ClInputScoped implements ClassLoadInput {

    /** Database context. */
    private final DSLContext ctx;

    /** Schedule identifier. */
    private final long schedule;

    /** Origin input. */
    private final ClassLoadInput origin;

    /**
     * New scoped input.
     *
     * @param ctx Database context
     * @param schedule Schedule identifier
     * @param origin Origin input
     */
    public ClInputScoped(
        final DSLContext ctx, final long schedule, final ClassLoadInput origin
    ) {
        this.ctx = ctx;
        this.schedule = schedule;
        this.origin = origin;
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        final Map<String, String> fields = this.origin.print(new MdClInput()).print();
        if (this.ctx.resultQuery(
            new ClasspathSql("class-load/validate-references.sql").text(), this.schedule,
            Long.valueOf(fields.get("schoolClassId")), Long.valueOf(fields.get("subjectId"))
        ).fetchOne() == null) {
            throw new LoadInputException("Choose an active class and subject from this school");
        }
        return this.origin.print(media);
    }
}
