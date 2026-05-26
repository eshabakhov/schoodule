/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.schedule.curriculum;

import com.eshabakhov.schoodule.school.SchoolClass;
import com.eshabakhov.schoodule.school.Subject;
import com.eshabakhov.schoodule.school.schedule.ClassCurriculum;
import com.eshabakhov.schoodule.school.schoolclass.ScPostgres;
import com.eshabakhov.schoodule.school.subject.SbPostgres;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jooq.DSLContext;

/**
 * Simple implementation of {@link ClassCurriculum}.
 *
 * @since 0.0.1
 */
public final class CsCrPostgres implements ClassCurriculum {

    /** JOOQ Table for ClassCurriculum. */
    private static final com.eshabakhov.schoodule.tables.ClassCurriculum CURRICULUM =
        com.eshabakhov.schoodule.tables.ClassCurriculum.CLASS_CURRICULUM;

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    /** Curriculum ID. */
    private final Long cid;

    public CsCrPostgres(final DSLContext ctx, final Long cid) {
        this.ctx = ctx;
        this.cid = cid;
    }

    @Override
    public Long uid() {
        return this.cid;
    }

    @Override
    public SchoolClass schoolClass() {
        return new ScPostgres(
            this.ctx,
            this.ctx.selectFrom(CsCrPostgres.CURRICULUM)
                .where(CsCrPostgres.CURRICULUM.ID.eq(this.cid))
                .fetchOne(CsCrPostgres.CURRICULUM.SCHOOL_CLASS_ID)
        );
    }

    @Override
    public Subject subject() {
        return new SbPostgres(
            this.ctx,
            this.ctx.selectFrom(CsCrPostgres.CURRICULUM)
                .where(CsCrPostgres.CURRICULUM.ID.eq(this.cid))
                .fetchOne(CsCrPostgres.CURRICULUM.SUBJECT_ID)
        );
    }

    @Override
    public Integer hoursPerWeek() {
        return this.ctx.selectFrom(CsCrPostgres.CURRICULUM)
            .where(CsCrPostgres.CURRICULUM.ID.eq(this.cid))
            .fetchOne(CsCrPostgres.CURRICULUM.HOURS_PER_WEEK);
    }

    @Override
    public ClassCurriculum teach(final Subject subject) {
        this.ctx.update(CsCrPostgres.CURRICULUM)
            .set(CsCrPostgres.CURRICULUM.SUBJECT_ID, subject.uid())
            .where(CsCrPostgres.CURRICULUM.ID.eq(this.cid))
            .execute();
        return new CsCrPostgres(this.ctx, this.cid);
    }

    @Override
    public ClassCurriculum target(final SchoolClass cls) {
        this.ctx.update(CsCrPostgres.CURRICULUM)
            .set(CsCrPostgres.CURRICULUM.SCHOOL_CLASS_ID, cls.uid())
            .where(CsCrPostgres.CURRICULUM.ID.eq(this.cid))
            .execute();
        return new CsCrPostgres(this.ctx, this.cid);
    }

    @Override
    public ClassCurriculum allocate(final Integer hours) {
        this.ctx.update(CsCrPostgres.CURRICULUM)
            .set(CsCrPostgres.CURRICULUM.HOURS_PER_WEEK, hours)
            .where(CsCrPostgres.CURRICULUM.ID.eq(this.cid))
            .execute();
        return new CsCrPostgres(this.ctx, this.cid);
    }

    @Override
    public ObjectNode json() {
        return this.ctx
            .select(
                CsCrPostgres.CURRICULUM.ID,
                CsCrPostgres.CURRICULUM.SCHOOL_CLASS_ID,
                CsCrPostgres.CURRICULUM.SUBJECT_ID,
                CsCrPostgres.CURRICULUM.HOURS_PER_WEEK
            )
            .from(CsCrPostgres.CURRICULUM)
            .where(CsCrPostgres.CURRICULUM.ID.eq(this.cid))
            .fetchOne(
                r -> {
                    final ObjectNode json = JsonNodeFactory.instance.objectNode();
                    json.put("id", r.get(CURRICULUM.ID));
                    json.set(
                        "schoolClass",
                        new ScPostgres(this.ctx, r.get(CURRICULUM.SCHOOL_CLASS_ID)).json()
                    );
                    json.set(
                        "subject",
                        new SbPostgres(this.ctx, r.get(CURRICULUM.SUBJECT_ID)).json()
                    );
                    json.put("hoursPerWeek", r.get(CURRICULUM.HOURS_PER_WEEK));
                    return json;
                }
            );
    }
}
