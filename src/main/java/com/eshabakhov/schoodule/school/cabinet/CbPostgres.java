/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.cabinet;

import com.eshabakhov.schoodule.school.Cabinet;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

/**
 * Postgres implementation of {@link Cabinet}.
 *
 * @since 0.0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class CbPostgres implements Cabinet {

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    /** Cabinet id. */
    private final long id;

    public CbPostgres(final DSLContext ctx, final long id) {
        this.ctx = ctx;
        this.id = id;
    }

    @Override
    public Long uid() {
        return this.id;
    }

    @Override
    public String name() {
        return this.ctx.select(DSL.field("name"))
            .from("public.cabinet")
            .where(DSL.condition("id = ?", this.id))
            .fetchOne("name", String.class);
    }

    @Override
    public ObjectNode json() {
        return this.ctx.select(DSL.field("id"), DSL.field("name"))
            .from("public.cabinet")
            .where(DSL.condition("id = ?", this.id))
            .fetchOne(
                rec ->
                    JsonNodeFactory.instance.objectNode()
                        .put("id", rec.get("id", Long.class))
                        .put("name", rec.get("name", String.class))
            );
    }
}
