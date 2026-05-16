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
    private final DSLContext datasource;

    /** School class id. */
    private final long clazzid;

    public ScPostgres(final DSLContext datasource, final long clazzid) {
        this.datasource = datasource;
        this.clazzid = clazzid;
    }

    @Override
    public Long uid() {
        return this.clazzid;
    }

    @Override
    public String name() {
        return this.datasource
            .select(
                DSL.concat(
                    ScPostgres.SCHOOL_CLASS.GRADE.cast(String.class),
                    ScPostgres.SCHOOL_CLASS.LITTER
                )
            )
            .from(ScPostgres.SCHOOL_CLASS)
            .where(ScPostgres.SCHOOL_CLASS.ID.eq(this.clazzid))
            .fetchOne(0, String.class);
    }

    @Override
    public Integer grade() {
        return this.datasource
            .select(ScPostgres.SCHOOL_CLASS.GRADE)
            .from(ScPostgres.SCHOOL_CLASS)
            .where(ScPostgres.SCHOOL_CLASS.ID.eq(this.clazzid))
            .fetchOneInto(Integer.class);
    }

    @Override
    public String litter() {
        return this.datasource
            .select(ScPostgres.SCHOOL_CLASS.LITTER)
            .from(ScPostgres.SCHOOL_CLASS)
            .where(ScPostgres.SCHOOL_CLASS.ID.eq(this.clazzid))
            .fetchOneInto(String.class);
    }

    @Override
    public ObjectNode json() {
        return this.datasource
            .select(
                ScPostgres.SCHOOL_CLASS.ID,
                ScPostgres.SCHOOL_CLASS.LITTER,
                ScPostgres.SCHOOL_CLASS.GRADE
            )
            .from(ScPostgres.SCHOOL_CLASS)
            .where(ScPostgres.SCHOOL_CLASS.ID.eq(this.clazzid))
            .fetchOne(
                clazz ->
                    JsonNodeFactory.instance.objectNode()
                        .put("id", clazz.get(ScPostgres.SCHOOL_CLASS.ID))
                        .put("grade", clazz.get(ScPostgres.SCHOOL_CLASS.GRADE))
                        .put("litter", clazz.get(ScPostgres.SCHOOL_CLASS.LITTER))
            );
    }
}
