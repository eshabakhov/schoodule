/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.schedule;

import com.eshabakhov.schoodule.school.Schedule;
import com.eshabakhov.schoodule.school.schedule.curriculum.PgClassCurriculums;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jooq.DSLContext;

/**
 * Postgres implementation of {@link Schedule}.
 *
 * @since 0.0.1
 */
public final class SdPostgres implements Schedule {

    /** JOOQ Table for Schedule. */
    private static final com.eshabakhov.schoodule.tables.Schedule SCHEDULE =
        com.eshabakhov.schoodule.tables.Schedule.SCHEDULE;

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    /** Schedule id. */
    private final Long sid;

    public SdPostgres(final DSLContext ctx, final Long sid) {
        this.ctx = ctx;
        this.sid = sid;
    }

    @Override
    public Long uid() {
        return this.sid;
    }

    @Override
    public String name() {
        return this.ctx
            .select(SdPostgres.SCHEDULE.NAME)
            .from(SdPostgres.SCHEDULE)
            .where(SdPostgres.SCHEDULE.ID.eq(this.sid))
            .fetchOneInto(String.class);
    }

    @Override
    public Schedule renamed(final String name) {
        return new SdPostgres(
            this.ctx,
            this.ctx.update(SdPostgres.SCHEDULE)
                .set(SdPostgres.SCHEDULE.NAME, name)
                .where(SdPostgres.SCHEDULE.ID.eq(this.sid))
                .returningResult(SdPostgres.SCHEDULE.ID)
                .fetchOne(SdPostgres.SCHEDULE.ID)
        );
    }

    @Override
    public ObjectNode json() {
        return this.ctx
            .select(SdPostgres.SCHEDULE.ID, SdPostgres.SCHEDULE.NAME)
            .from(SdPostgres.SCHEDULE)
            .where(SdPostgres.SCHEDULE.ID.eq(this.sid))
            .fetchOne(
                clazz ->
                    JsonNodeFactory.instance.objectNode()
                        .put("id", clazz.get(SdPostgres.SCHEDULE.ID))
                        .put("name", clazz.get(SdPostgres.SCHEDULE.NAME))
            );
    }

    @Override
    public ClassCurriculums curriculums() {
        return new PgClassCurriculums(this.ctx, this.sid);
    }
}
