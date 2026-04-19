/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.teacher;

import com.eshabakhov.schoodule.school.Teacher;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jooq.DSLContext;

/**
 * Simple implementation of {@link Teacher}.
 *
 * @since 0.0.1
 */
public final class ThPostgres implements Teacher {

    /** JOOQ Table for Teacher. */
    private static final com.eshabakhov.schoodule.tables.Teacher TEACHER =
        com.eshabakhov.schoodule.tables.Teacher.TEACHER;

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext datasource;

    /** Teacher ID. */
    private final long tid;

    public ThPostgres(final DSLContext datasource, final Long tid) {
        this.datasource = datasource;
        this.tid = tid;
    }

    @Override
    public Long uid() {
        return this.tid;
    }

    @Override
    public String name() {
        return this.datasource
            .select(ThPostgres.TEACHER.NAME)
            .from(ThPostgres.TEACHER)
            .where(ThPostgres.TEACHER.ID.eq(this.tid))
            .fetchOneInto(String.class);
    }

    @Override
    public ObjectNode json() {
        return this.datasource
            .select(ThPostgres.TEACHER.ID, ThPostgres.TEACHER.NAME)
            .from(ThPostgres.TEACHER)
            .where(ThPostgres.TEACHER.ID.eq(this.tid))
            .fetchOne(
                clazz ->
                    JsonNodeFactory.instance.objectNode()
                        .put("id", clazz.get(ThPostgres.TEACHER.ID))
                        .put("name", clazz.get(ThPostgres.TEACHER.NAME))
            );
    }
}
