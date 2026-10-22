/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.building.cabinet;

import com.eshabakhov.surok.school.building.Cabinet;
import com.eshabakhov.surok.sql.ClasspathSql;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jooq.DSLContext;

/**
 * Postgres implementation of {@link Cabinet}.
 *
 * @since 0.0.1
 */
public final class CbPostgres implements Cabinet {

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    /** Cabinet id. */
    private final Long id;

    /**
     * New cabinet.
     *
     * @param ctx Database context
     * @param id Cabinet ID
     * @since 0.0.1
     */
    public CbPostgres(final DSLContext ctx, final Long id) {
        this.ctx = ctx;
        this.id = id;
    }

    @Override
    public Long uid() {
        return this.id;
    }

    @Override
    public String name() {
        return this.ctx.resultQuery(
            new ClasspathSql("cabinet/find-name.sql").text(),
            this.id
        ).fetchOneInto(String.class);
    }

    @Override
    public Cabinet renamed(final String name) {
        return new CbPostgres(
            this.ctx,
            this.ctx.resultQuery(
                new ClasspathSql("cabinet/rename.sql").text(),
                name,
                this.id
            ).fetchOne("id", Long.class)
        );
    }

    @Override
    public ObjectNode json() {
        return this.ctx.resultQuery(
            new ClasspathSql("cabinet/find-details.sql").text(),
            this.id
        ).fetchOne(
            clazz ->
                JsonNodeFactory.instance.objectNode()
                    .put("id", clazz.get("id", Long.class))
                    .put("name", clazz.get("name", String.class))
        );
    }
}
