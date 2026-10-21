/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.teacher;

import com.eshabakhov.surok.school.Teacher;
import com.eshabakhov.surok.sql.ClasspathSql;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jooq.DSLContext;

/**
 * Simple implementation of {@link Teacher}.
 *
 * @since 0.0.1
 */
public final class ThPostgres implements Teacher {

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    /** Teacher ID. */
    private final long tid;

    /**
     * New teacher.
     *
     * @param ctx Database context
     * @param tid Teacher ID
     * @since 0.0.1
     */
    public ThPostgres(final DSLContext ctx, final Long tid) {
        this.ctx = ctx;
        this.tid = tid;
    }

    @Override
    public Long uid() {
        return this.tid;
    }

    @Override
    public String name() {
        return this.ctx.resultQuery(
            new ClasspathSql("teacher/find-name.sql").text(),
            this.tid
        ).fetchOneInto(String.class);
    }

    @Override
    public Teacher renamed(final String name) {
        return new ThPostgres(
            this.ctx,
            this.ctx.resultQuery(
                new ClasspathSql("teacher/rename.sql").text(),
                name,
                this.tid
            ).fetchOne("id", Long.class)
        );
    }

    @Override
    public ObjectNode json() {
        return this.ctx.resultQuery(
            new ClasspathSql("teacher/find-details.sql").text(),
            this.tid
        ).fetchOne(
            clazz ->
                JsonNodeFactory.instance.objectNode()
                    .put("id", clazz.get("id", Long.class))
                    .put("name", clazz.get("name", String.class))
        );
    }
}
