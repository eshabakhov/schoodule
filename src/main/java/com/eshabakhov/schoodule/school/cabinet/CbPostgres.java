/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.cabinet;

import com.eshabakhov.schoodule.school.Cabinet;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jooq.DSLContext;

/**
 * Postgres implementation of {@link Cabinet}.
 *
 * @since 0.0.1
 */
public final class CbPostgres implements Cabinet {

    /** JOOQ Table for Cabinet. */
    private static final com.eshabakhov.schoodule.tables.Cabinet CABINET =
        com.eshabakhov.schoodule.tables.Cabinet.CABINET;

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    /** Cabinet id. */
    private final Long id;

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
        return this.ctx
            .select(CbPostgres.CABINET.NAME)
            .from(CbPostgres.CABINET)
            .where(CbPostgres.CABINET.ID.eq(this.id))
            .fetchOneInto(String.class);
    }

    @Override
    public Cabinet renamed(final String name) {
        return new CbPostgres(
            this.ctx,
            this.ctx.update(CbPostgres.CABINET)
                .set(CbPostgres.CABINET.NAME, name)
                .where(CbPostgres.CABINET.ID.eq(this.id))
                .returningResult(CbPostgres.CABINET.ID)
                .fetchOne(CbPostgres.CABINET.ID)
        );
    }

    @Override
    public ObjectNode json() {
        return this.ctx
            .select(CbPostgres.CABINET.ID, CbPostgres.CABINET.NAME)
            .from(CbPostgres.CABINET)
            .where(CbPostgres.CABINET.ID.eq(this.id))
            .fetchOne(
                clazz ->
                    JsonNodeFactory.instance.objectNode()
                        .put("id", clazz.get(CbPostgres.CABINET.ID))
                        .put("name", clazz.get(CbPostgres.CABINET.NAME))
            );
    }
}
