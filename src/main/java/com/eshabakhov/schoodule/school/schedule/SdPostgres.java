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
    private final DSLContext datasource;

    /** Schedule id. */
    private final Long sid;

    public SdPostgres(final DSLContext datasource, final Long sid) {
        this.datasource = datasource;
        this.sid = sid;
    }

    @Override
    public Long uid() {
        return this.sid;
    }

    @Override
    public String name() {
        return this.datasource
            .select(SdPostgres.SCHEDULE.NAME)
            .from(SdPostgres.SCHEDULE)
            .where(SdPostgres.SCHEDULE.ID.eq(this.sid))
            .fetchOneInto(String.class);
    }

    @Override
    public ObjectNode json() {
        return this.datasource
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
        return new PgClassCurriculums(this.datasource, this.sid);
    }
}
