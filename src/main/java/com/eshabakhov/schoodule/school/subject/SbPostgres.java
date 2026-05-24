/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.subject;

import com.eshabakhov.schoodule.school.Subject;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jooq.DSLContext;

/**
 * Postgres implementation of {@link Subject}.
 *
 * @since 0.0.1
 */
public final class SbPostgres implements Subject {

    /** JOOQ Table for Cabinet. */
    private static final com.eshabakhov.schoodule.tables.Subject SUBJECT =
        com.eshabakhov.schoodule.tables.Subject.SUBJECT;

    /** Subject id. */
    private final Long sid;

    /** Database connection. */
    private final DSLContext ctx;

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
        return this.ctx
            .select(SbPostgres.SUBJECT.NAME)
            .from(SbPostgres.SUBJECT)
            .where(SbPostgres.SUBJECT.ID.eq(this.sid))
            .fetchOneInto(String.class);
    }

    @Override
    public Subject renamed(final String name) {
        return new SbPostgres(
            this.ctx,
            this.ctx.update(SbPostgres.SUBJECT)
                .set(SbPostgres.SUBJECT.NAME, name)
                .where(SbPostgres.SUBJECT.ID.eq(this.sid))
                .returningResult(SbPostgres.SUBJECT.ID)
                .fetchOne(SbPostgres.SUBJECT.ID)
        );
    }

    @Override
    public ObjectNode json() {
        return this.ctx
            .select(SbPostgres.SUBJECT.ID, SbPostgres.SUBJECT.NAME)
            .from(SbPostgres.SUBJECT)
            .where(SbPostgres.SUBJECT.ID.eq(this.sid))
            .fetchOne(
                clazz ->
                    JsonNodeFactory.instance.objectNode()
                        .put("id", clazz.get(SbPostgres.SUBJECT.ID))
                        .put("name", clazz.get(SbPostgres.SUBJECT.NAME))
            );
    }
}
