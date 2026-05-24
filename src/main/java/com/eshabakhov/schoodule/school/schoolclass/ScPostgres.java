/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.schoolclass;

import com.eshabakhov.schoodule.school.SchoolClass;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

/**
 * Postgres implementation of {@link SchoolClass}.
 *
 * @since 0.0.1
 */
public final class ScPostgres implements SchoolClass {

    /** JOOQ Table for Schedule. */
    private static final com.eshabakhov.schoodule.tables.SchoolClass SCHOOL_CLASS =
        com.eshabakhov.schoodule.tables.SchoolClass.SCHOOL_CLASS;

    /** Database context. */
    private final DSLContext ctx;

    /** School class id. */
    private final Long clazzid;

    public ScPostgres(final DSLContext ctx, final Long clazzid) {
        this.ctx = ctx;
        this.clazzid = clazzid;
    }

    @Override
    public Long uid() {
        return this.clazzid;
    }

    @Override
    public String name() {
        return this.ctx
            .select(
                DSL.concat(
                    ScPostgres.SCHOOL_CLASS.GRADE.cast(String.class),
                    ScPostgres.SCHOOL_CLASS.LITERA
                )
            )
            .from(ScPostgres.SCHOOL_CLASS)
            .where(ScPostgres.SCHOOL_CLASS.ID.eq(this.clazzid))
            .fetchOne(0, String.class);
    }

    @Override
    public Integer grade() {
        return this.ctx
            .select(ScPostgres.SCHOOL_CLASS.GRADE)
            .from(ScPostgres.SCHOOL_CLASS)
            .where(ScPostgres.SCHOOL_CLASS.ID.eq(this.clazzid))
            .fetchOneInto(Integer.class);
    }

    @Override
    public String litera() {
        return this.ctx
            .select(ScPostgres.SCHOOL_CLASS.LITERA)
            .from(ScPostgres.SCHOOL_CLASS)
            .where(ScPostgres.SCHOOL_CLASS.ID.eq(this.clazzid))
            .fetchOneInto(String.class);
    }

    @Override
    public SchoolClass regraded(final Integer grade) {
        return new ScPostgres(
            this.ctx,
            this.ctx.update(ScPostgres.SCHOOL_CLASS)
                .set(ScPostgres.SCHOOL_CLASS.GRADE, grade)
                .where(ScPostgres.SCHOOL_CLASS.ID.eq(this.clazzid))
                .returningResult(ScPostgres.SCHOOL_CLASS.ID)
                .fetchOne(ScPostgres.SCHOOL_CLASS.ID)
        );
    }

    @Override
    public SchoolClass reliterated(final String litera) {
        return new ScPostgres(
            this.ctx,
            this.ctx.update(ScPostgres.SCHOOL_CLASS)
                .set(ScPostgres.SCHOOL_CLASS.LITERA, litera)
                .where(ScPostgres.SCHOOL_CLASS.ID.eq(this.clazzid))
                .returningResult(ScPostgres.SCHOOL_CLASS.ID)
                .fetchOne(ScPostgres.SCHOOL_CLASS.ID)
        );
    }

    @Override
    public ObjectNode json() {
        return this.ctx
            .select(
                ScPostgres.SCHOOL_CLASS.ID,
                ScPostgres.SCHOOL_CLASS.LITERA,
                ScPostgres.SCHOOL_CLASS.GRADE
            )
            .from(ScPostgres.SCHOOL_CLASS)
            .where(ScPostgres.SCHOOL_CLASS.ID.eq(this.clazzid))
            .fetchOne(
                clazz ->
                    JsonNodeFactory.instance.objectNode()
                        .put("id", clazz.get(ScPostgres.SCHOOL_CLASS.ID))
                        .put("grade", clazz.get(ScPostgres.SCHOOL_CLASS.GRADE))
                        .put("litera", clazz.get(ScPostgres.SCHOOL_CLASS.LITERA))
            );
    }
}
