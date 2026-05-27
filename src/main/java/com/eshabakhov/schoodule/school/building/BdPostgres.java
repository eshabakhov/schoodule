/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.building;

import com.eshabakhov.schoodule.school.Building;
import com.eshabakhov.schoodule.school.building.cabinet.CbsPostgres;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jooq.DSLContext;

/**
 * Postgres implementation of {@link Building}.
 *
 * @since 0.0.1
 */
public final class BdPostgres implements Building {

    /** JOOQ Table for Building. */
    private static final com.eshabakhov.schoodule.tables.Building BUILDING =
        com.eshabakhov.schoodule.tables.Building.BUILDING;

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    /** Building id. */
    private final Long id;

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
        return this.ctx
            .select(BdPostgres.BUILDING.NAME)
            .from(BdPostgres.BUILDING)
            .where(BdPostgres.BUILDING.ID.eq(this.id))
            .fetchOneInto(String.class);
    }

    @Override
    public Building renamed(final String name) {
        return new BdPostgres(
            this.ctx,
            this.ctx.update(BdPostgres.BUILDING)
                .set(BdPostgres.BUILDING.NAME, name)
                .where(BdPostgres.BUILDING.ID.eq(this.id))
                .returningResult(BdPostgres.BUILDING.ID)
                .fetchOne(BdPostgres.BUILDING.ID)
        );
    }

    @Override
    public Cabinets cabinets() {
        return new CbsPostgres(this.ctx, this.id);
    }

    @Override
    public ObjectNode json() {
        return this.ctx
            .select(BdPostgres.BUILDING.ID, BdPostgres.BUILDING.NAME)
            .from(BdPostgres.BUILDING)
            .where(BdPostgres.BUILDING.ID.eq(this.id))
            .fetchOne(
                clazz ->
                    JsonNodeFactory.instance.objectNode()
                        .put("id", clazz.get(BdPostgres.BUILDING.ID))
                        .put("name", clazz.get(BdPostgres.BUILDING.NAME))
            );
    }
}
