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
    private final DSLContext datasource;

    /** Cabinet id. */
    private final long cid;

    public CbPostgres(final DSLContext datasource, final long cid) {
        this.datasource = datasource;
        this.cid = cid;
    }

    @Override
    public Long uid() {
        return this.cid;
    }

    @Override
    public String name() {
        return this.datasource
            .select(CbPostgres.CABINET.NAME)
            .from(CbPostgres.CABINET)
            .where(CbPostgres.CABINET.ID.eq(this.cid))
            .fetchOneInto(String.class);
    }

    @Override
    public ObjectNode json() {
        return this.datasource
            .select(CbPostgres.CABINET.ID, CbPostgres.CABINET.NAME)
            .from(CbPostgres.CABINET)
            .where(CbPostgres.CABINET.ID.eq(this.cid))
            .fetchOne(
                clazz ->
                    JsonNodeFactory.instance.objectNode()
                        .put("id", clazz.get(CbPostgres.CABINET.ID))
                        .put("name", clazz.get(CbPostgres.CABINET.NAME))
            );
    }
}
