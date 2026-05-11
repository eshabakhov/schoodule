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
        return this.ctx
            .fetchOne(
                """
                SELECT name
                FROM public.cabinet
                WHERE id = ?
                """,
                this.id
            )
            .get("name", String.class);
    }

    @Override
    public ObjectNode json() {
        return this.ctx
            .fetchOne(
                """
                SELECT id, name
                FROM public.cabinet
                WHERE id = ?
                """,
                this.id
            )
            .map(
                r ->
                    JsonNodeFactory.instance.objectNode()
                        .put("id", r.get("id", Long.class))
                        .put("name", r.get("name", String.class))
            );
    }
}
