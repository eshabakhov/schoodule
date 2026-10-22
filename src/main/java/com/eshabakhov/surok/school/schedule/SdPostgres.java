/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.schedule;

import com.eshabakhov.surok.school.Schedule;
import com.eshabakhov.surok.school.schedule.load.ClassLoads;
import com.eshabakhov.surok.school.schedule.load.ClassLoadsPostgres;
import com.eshabakhov.surok.sql.ClasspathSql;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jooq.DSLContext;

/**
 * Postgres implementation of {@link Schedule}.
 *
 * @since 0.0.1
 */
public final class SdPostgres implements Schedule {

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    /** Schedule id. */
    private final Long sid;

    /**
     * New schedule.
     *
     * @param ctx Database context
     * @param sid Schedule ID
     * @since 0.0.1
     */
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
        return this.ctx.resultQuery(
            new ClasspathSql("schedule/find-name.sql").text(),
            this.sid
        ).fetchOneInto(String.class);
    }

    @Override
    public Schedule renamed(final String name) {
        return new SdPostgres(
            this.ctx,
            this.ctx.resultQuery(
                new ClasspathSql("schedule/rename.sql").text(),
                name,
                this.sid
            ).fetchOne("id", Long.class)
        );
    }

    @Override
    public ObjectNode json() {
        return this.ctx.resultQuery(
            new ClasspathSql("schedule/find-details.sql").text(),
            this.sid
        ).fetchOne(
            clazz ->
                JsonNodeFactory.instance.objectNode()
                    .put("id", clazz.get("id", Long.class))
                    .put("name", clazz.get("name", String.class))
        );
    }

    @Override
    public ClassLoads loads() {
        return new ClassLoadsPostgres(this.ctx, this.sid);
    }
}
