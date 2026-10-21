/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.subject;

import com.eshabakhov.surok.school.Subject;
import com.eshabakhov.surok.sql.ClasspathSql;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jooq.DSLContext;

/**
 * Postgres implementation of {@link Subject}.
 *
 * @since 0.0.1
 */
public final class SbPostgres implements Subject {

    /** Subject id. */
    private final Long sid;

    /** Database connection. */
    private final DSLContext ctx;

    /**
     * New subject.
     *
     * @param ctx Database context
     * @param sid Subject ID
     * @since 0.0.1
     */
    public SbPostgres(final DSLContext ctx, final Long sid) {
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
            new ClasspathSql("subject/find-name.sql").text(),
            this.sid
        ).fetchOneInto(String.class);
    }

    @Override
    public Subject renamed(final String name) {
        return new SbPostgres(
            this.ctx,
            this.ctx.resultQuery(
                new ClasspathSql("subject/rename.sql").text(),
                name,
                this.sid
            ).fetchOne("id", Long.class)
        );
    }

    @Override
    public ObjectNode json() {
        return this.ctx.resultQuery(
            new ClasspathSql("subject/find-details.sql").text(),
            this.sid
        ).fetchOne(
            clazz ->
                JsonNodeFactory.instance.objectNode()
                    .put("id", clazz.get("id", Long.class))
                    .put("name", clazz.get("name", String.class))
        );
    }
}
