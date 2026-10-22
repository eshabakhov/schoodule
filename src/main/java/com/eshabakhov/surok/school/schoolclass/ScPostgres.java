/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.schoolclass;

import com.eshabakhov.surok.school.SchoolClass;
import com.eshabakhov.surok.sql.ClasspathSql;
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

    /** Database context. */
    private final DSLContext ctx;

    /** School class id. */
    private final Long clazzid;

    /**
     * New school class.
     *
     * @param ctx Database context
     * @param clazzid School class ID
     * @since 0.0.1
     */
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
        return this.ctx.resultQuery(
            new ClasspathSql("school-class/find-name.sql").text(),
            DSL.concat(
                DSL.field(
                    DSL.name("public", "school_class", "grade"),
                    Integer.class
                ).cast(String.class),
                DSL.field(DSL.name("public", "school_class", "litera"), String.class)
            ),
            DSL.val(this.clazzid)
        ).fetchOne(0, String.class);
    }

    @Override
    public Integer grade() {
        return this.ctx.resultQuery(
            new ClasspathSql("school-class/find-grade.sql").text(),
            this.clazzid
        ).fetchOneInto(Integer.class);
    }

    @Override
    public String litera() {
        return this.ctx.resultQuery(
            new ClasspathSql("school-class/find-litera.sql").text(),
            this.clazzid
        ).fetchOneInto(String.class);
    }

    @Override
    public SchoolClass regraded(final Integer grade) {
        return new ScPostgres(
            this.ctx,
            this.ctx.resultQuery(
                new ClasspathSql("school-class/update-grade.sql").text(),
                grade,
                this.clazzid
            ).fetchOne("id", Long.class)
        );
    }

    @Override
    public SchoolClass reliterated(final String litera) {
        return new ScPostgres(
            this.ctx,
            this.ctx.resultQuery(
                new ClasspathSql("school-class/update-litera.sql").text(),
                litera,
                this.clazzid
            ).fetchOne("id", Long.class)
        );
    }

    @Override
    public ObjectNode json() {
        return this.ctx.resultQuery(
            new ClasspathSql("school-class/find-details.sql").text(),
            this.clazzid
        ).fetchOne(
            clazz ->
                JsonNodeFactory.instance.objectNode()
                    .put("id", clazz.get("id", Long.class))
                    .put("grade", clazz.get("grade", Integer.class))
                    .put("litera", clazz.get("litera", String.class))
        );
    }
}
