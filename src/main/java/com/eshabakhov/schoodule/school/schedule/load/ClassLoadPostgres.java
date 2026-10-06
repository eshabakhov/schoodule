/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.schedule.load;

import com.eshabakhov.schoodule.school.SchoolClass;
import com.eshabakhov.schoodule.school.Subject;
import com.eshabakhov.schoodule.school.schoolclass.ScPostgres;
import com.eshabakhov.schoodule.school.subject.SbPostgres;
import com.eshabakhov.schoodule.tables.ClassCurriculum;
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

    /** JOOQ table for class loads. */
    private static final ClassCurriculum LOAD =
        ClassCurriculum.CLASS_CURRICULUM;

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
            this.ctx.selectFrom(ClassLoadPostgres.LOAD)
                .where(ClassLoadPostgres.LOAD.ID.eq(this.lid))
                .fetchOne(ClassLoadPostgres.LOAD.SCHOOL_CLASS_ID)
        );
    }

    @Override
    public Subject subject() {
        return new SbPostgres(
            this.ctx,
            this.ctx.selectFrom(ClassLoadPostgres.LOAD)
                .where(ClassLoadPostgres.LOAD.ID.eq(this.lid))
                .fetchOne(ClassLoadPostgres.LOAD.SUBJECT_ID)
        );
    }

    @Override
    public Integer hoursPerWeek() {
        return this.ctx.selectFrom(ClassLoadPostgres.LOAD)
            .where(ClassLoadPostgres.LOAD.ID.eq(this.lid))
            .fetchOne(ClassLoadPostgres.LOAD.HOURS_PER_WEEK);
    }

    @Override
    public ClassLoad teach(final Subject subject) {
        this.ctx.update(ClassLoadPostgres.LOAD)
            .set(ClassLoadPostgres.LOAD.SUBJECT_ID, subject.uid())
            .where(ClassLoadPostgres.LOAD.ID.eq(this.lid))
            .execute();
        return new ClassLoadPostgres(this.ctx, this.lid);
    }

    @Override
    public ClassLoad target(final SchoolClass cls) {
        this.ctx.update(ClassLoadPostgres.LOAD)
            .set(ClassLoadPostgres.LOAD.SCHOOL_CLASS_ID, cls.uid())
            .where(ClassLoadPostgres.LOAD.ID.eq(this.lid))
            .execute();
        return new ClassLoadPostgres(this.ctx, this.lid);
    }

    @Override
    public ClassLoad allocate(final Integer hours) {
        this.ctx.update(ClassLoadPostgres.LOAD)
            .set(ClassLoadPostgres.LOAD.HOURS_PER_WEEK, hours)
            .where(ClassLoadPostgres.LOAD.ID.eq(this.lid))
            .execute();
        return new ClassLoadPostgres(this.ctx, this.lid);
    }

    @Override
    public ObjectNode json() {
        return this.ctx.select(
            ClassLoadPostgres.LOAD.ID,
            ClassLoadPostgres.LOAD.SCHOOL_CLASS_ID,
            ClassLoadPostgres.LOAD.SUBJECT_ID,
            ClassLoadPostgres.LOAD.HOURS_PER_WEEK
            )
            .from(ClassLoadPostgres.LOAD)
            .where(ClassLoadPostgres.LOAD.ID.eq(this.lid)).fetchOne(
                r -> {
                    final ObjectNode json = JsonNodeFactory.instance.objectNode();
                    json.put("id", r.get(ClassLoadPostgres.LOAD.ID));
                    json.set(
                        "schoolClass",
                        new ScPostgres(
                            this.ctx,
                            r.get(ClassLoadPostgres.LOAD.SCHOOL_CLASS_ID)
                        ).json()
                    );
                    json.set(
                        "subject",
                        new SbPostgres(
                            this.ctx,
                            r.get(ClassLoadPostgres.LOAD.SUBJECT_ID)
                        ).json()
                    );
                    json.put(
                        "hoursPerWeek",
                        r.get(ClassLoadPostgres.LOAD.HOURS_PER_WEEK)
                    );
                    return json;
                }
            );
    }
}
