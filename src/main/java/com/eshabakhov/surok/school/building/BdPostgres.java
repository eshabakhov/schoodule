/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.building;

import com.eshabakhov.surok.school.Building;
import com.eshabakhov.surok.school.building.cabinet.CbsPostgres;
import com.eshabakhov.surok.sql.ClasspathSql;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jooq.DSLContext;

/**
 * Postgres implementation of {@link Building}.
 *
 * @since 0.0.1
 */
public final class BdPostgres implements Building {

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    /** Building id. */
    private final Long id;

    /**
     * New building.
     *
     * @param ctx Database context
     * @param id Building ID
     * @since 0.0.1
     */
    public BdPostgres(final DSLContext ctx, final Long id) {
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
            new ClasspathSql("building/find-name.sql").text(),
            this.id
        ).fetchOneInto(String.class);
    }

    @Override
    public Building renamed(final String name) {
        return new BdPostgres(
            this.ctx,
            this.ctx.resultQuery(
                new ClasspathSql("building/rename.sql").text(),
                name,
                this.id
            ).fetchOne("id", Long.class)
        );
    }

    @Override
    public Cabinets cabinets() {
        return new CbsPostgres(this.ctx, this.id);
    }

    @Override
    public ObjectNode json() {
        return this.ctx.resultQuery(
            new ClasspathSql("building/find-details.sql").text(),
            this.id
        ).fetchOne(
            clazz ->
                JsonNodeFactory.instance.objectNode()
                    .put("id", clazz.get("id", Long.class))
                    .put("name", clazz.get("name", String.class))
        );
    }
}
