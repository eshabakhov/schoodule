/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.schedule.load;

import com.eshabakhov.surok.school.SchoolClass;
import com.eshabakhov.surok.school.Subject;
import com.eshabakhov.surok.school.schoolclass.ScPostgres;
import com.eshabakhov.surok.school.subject.SbPostgres;
import com.eshabakhov.surok.sql.ClasspathSql;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jooq.DSLContext;

/**
 * Postgres implementation of {@link ClassLoad}.
 *
 * @since 0.0.1
 * @checkstyle LambdaBodyLengthCheck (1000 lines)
 */
public final class ClassLoadPostgres implements ClassLoad {

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    /** Load identifier. */
    private final Long lid;

    /**
     * New class load.
     *
     * @param ctx Database context
     * @param lid Load ID
     * @since 0.0.1
     */
    public ClassLoadPostgres(final DSLContext ctx, final Long lid) {
        this.ctx = ctx;
        this.lid = lid;
    }

    @Override
    public Long uid() {
        return this.lid;
    }

    @Override
    public SchoolClass schoolClass() {
        return new ScPostgres(
            this.ctx,
            this.ctx.resultQuery(
                new ClasspathSql("class-load/find-by-id.sql").text(),
                this.lid
            ).fetchOne("school_class_id", Long.class)
        );
    }

    @Override
    public Subject subject() {
        return new SbPostgres(
            this.ctx,
            this.ctx.resultQuery(
                new ClasspathSql("class-load/find-by-id.sql").text(),
                this.lid
            ).fetchOne("subject_id", Long.class)
        );
    }

    @Override
    public Integer hoursPerWeek() {
        return this.ctx.resultQuery(
            new ClasspathSql("class-load/find-by-id.sql").text(),
            this.lid
        ).fetchOne("hours_per_week", Integer.class);
    }

    @Override
    public ClassLoad teach(final Subject subject) {
        this.ctx.resultQuery(
            new ClasspathSql("class-load/teach.sql").text(),
            subject.uid(),
            this.lid
        ).execute();
        return new ClassLoadPostgres(this.ctx, this.lid);
    }

    @Override
    public ClassLoad target(final SchoolClass cls) {
        this.ctx.resultQuery(
            new ClasspathSql("class-load/target.sql").text(),
            cls.uid(),
            this.lid
        ).execute();
        return new ClassLoadPostgres(this.ctx, this.lid);
    }

    @Override
    public ClassLoad allocate(final Integer hours) {
        this.ctx.resultQuery(
            new ClasspathSql("class-load/allocate.sql").text(),
            hours,
            this.lid
        ).execute();
        return new ClassLoadPostgres(this.ctx, this.lid);
    }

    @Override
    public ObjectNode json() {
        return this.ctx.resultQuery(
            new ClasspathSql("class-load/find-details.sql").text(),
            this.lid
        ).fetchOne(
            r -> {
                final ObjectNode json = JsonNodeFactory.instance.objectNode();
                json.put("id", r.get("id", Long.class));
                json.set(
                    "schoolClass",
                    new ScPostgres(
                        this.ctx,
                        r.get("school_class_id", Long.class)
                    ).json()
                );
                json.set(
                    "subject",
                    new SbPostgres(
                        this.ctx,
                        r.get("subject_id", Long.class)
                    ).json()
                );
                json.put(
                    "hoursPerWeek",
                    r.get("hours_per_week", Integer.class)
                );
                return json;
            }
        );
    }
}
