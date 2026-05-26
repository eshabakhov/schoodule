/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.schedule.curriculum;

import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.page.ResponsePageableList;
import com.eshabakhov.schoodule.school.SchoolClass;
import com.eshabakhov.schoodule.school.Subject;
import com.eshabakhov.schoodule.school.schedule.ClassCurriculum;
import com.eshabakhov.schoodule.school.schedule.ClassCurriculums;
import com.eshabakhov.schoodule.tables.records.ClassCurriculumRecord;
import lombok.EqualsAndHashCode;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

/**
 * Postgres implementation of {@link ClassCurriculums}.
 *
 * @since 0.0.1
 */
@EqualsAndHashCode
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public final class CsCrsPostgres implements ClassCurriculums {

    /** JOOQ Table for ClassCurriculum. */
    private static final com.eshabakhov.schoodule.tables.ClassCurriculum CURRICULUM =
        com.eshabakhov.schoodule.tables.ClassCurriculum.CLASS_CURRICULUM;

    /** JOOQ School class table. */
    private static final com.eshabakhov.schoodule.tables.SchoolClass CLASS =
        com.eshabakhov.schoodule.tables.SchoolClass.SCHOOL_CLASS;

    /** JOOQ Subject table. */
    private static final com.eshabakhov.schoodule.tables.Subject SUBJECT =
        com.eshabakhov.schoodule.tables.Subject.SUBJECT;

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    /** Schedule ID. */
    private final Long sid;

    public CsCrsPostgres(final DSLContext ctx, final Long sid) {
        this.ctx = ctx;
        this.sid = sid;
    }

    @Override
    public ClassCurriculum create(
        final SchoolClass clazz,
        final Subject subject,
        final Integer hours
    ) throws Exception {
        return this.ctx.transactionResult(
            config -> {
                final DSLContext ttx = DSL.using(config);
                final var select = ttx.selectFrom(CsCrsPostgres.CURRICULUM)
                    .where(
                        CsCrsPostgres.CURRICULUM.SCHEDULE_ID.eq(this.sid)
                            .and(
                                CsCrsPostgres.CURRICULUM.SCHOOL_CLASS_ID.eq(
                                    clazz.uid()
                                )
                            )
                            .and(
                                CsCrsPostgres.CURRICULUM.SUBJECT_ID.eq(
                                    subject.uid()
                                )
                            )
                    )
                    .fetchOne();
                if (select == null) {
                    final var created = ttx.insertInto(CsCrsPostgres.CURRICULUM)
                        .set(CsCrsPostgres.CURRICULUM.SCHEDULE_ID, this.sid)
                        .set(
                            CsCrsPostgres.CURRICULUM.SCHOOL_CLASS_ID,
                            clazz.uid()
                        )
                        .set(CsCrsPostgres.CURRICULUM.SUBJECT_ID, subject.uid())
                        .set(
                            CsCrsPostgres.CURRICULUM.HOURS_PER_WEEK,
                            hours
                        )
                        .returning()
                        .fetchOne();
                    if (created == null) {
                        throw new CurriculumFailedCreateException();
                    }
                    return new CsCrPostgres(this.ctx, created.getId());
                } else {
                    throw new CurriculumAlreadyExistsException();
                }
            }
        );
    }

    @Override
    public ClassCurriculum curriculum(final long cid) throws Exception {
        final var selected = this.ctx.selectFrom(CsCrsPostgres.CURRICULUM)
            .where(
                CsCrsPostgres.CURRICULUM.ID.eq(cid)
                    .and(CsCrsPostgres.CURRICULUM.SCHEDULE_ID.eq(this.sid))
            )
            .fetchOne();
        if (selected == null) {
            throw new CurriculumNotFoundException(
                String.format("ClassCurriculum with id=%d not found", cid)
            );
        }
        return new CsCrPostgres(this.ctx, selected.getId());
    }

    @Override
    public PageableList<ClassCurriculum> list(
        final Condition condition,
        final Page page
    ) throws Exception {
        return new ResponsePageableList<>(
            this.ctx
                .select(
                    CsCrsPostgres.CURRICULUM.ID,
                    CsCrsPostgres.CLASS.ID,
                    CsCrsPostgres.SUBJECT.ID,
                    CsCrsPostgres.CURRICULUM.HOURS_PER_WEEK
                )
                .from(CsCrsPostgres.CURRICULUM)
                .join(CsCrsPostgres.CLASS)
                .on(
                    CsCrsPostgres.CURRICULUM.SCHOOL_CLASS_ID.eq(
                        CsCrsPostgres.CLASS.ID
                    )
                )
                .join(CsCrsPostgres.SUBJECT)
                .on(
                    CsCrsPostgres.CURRICULUM.SUBJECT_ID.eq(
                        CsCrsPostgres.SUBJECT.ID
                    )
                )
                .where(CsCrsPostgres.CURRICULUM.SCHEDULE_ID.eq(this.sid))
                .fetch(
                    selected ->
                        new CsCrPostgres(this.ctx, selected.get(CsCrsPostgres.CURRICULUM.ID))
                ),
            this.ctx
                .fetchCount(
                    this.ctx.selectFrom(CsCrsPostgres.CURRICULUM)
                        .where(CsCrsPostgres.CURRICULUM.SCHEDULE_ID.eq(this.sid))
                ),
            page
        );
    }

    @Override
    public void remove(final long cid) throws Exception {
        final ClassCurriculumRecord curriculum = this.ctx
            .selectFrom(CsCrsPostgres.CURRICULUM)
            .where(
                CsCrsPostgres.CURRICULUM.ID.eq(cid)
                    .and(CsCrsPostgres.CURRICULUM.SCHEDULE_ID.eq(this.sid))
            )
            .fetchOne();
        if (curriculum == null) {
            throw new CurriculumNotFoundException(
                String.format("ClassCurriculum with id=%d not found", cid)
            );
        }
        this.ctx.transactionResult(
            config ->
                DSL.using(config).deleteFrom(CsCrsPostgres.CURRICULUM)
                    .where(CsCrsPostgres.CURRICULUM.ID.eq(cid))
                    .execute()
        );
    }

    public static class CurriculumFailedCreateException extends Exception {
        public CurriculumFailedCreateException() {
            super("Failed to create ClassCurriculum");
        }
    }

    public static class CurriculumAlreadyExistsException extends Exception {
        public CurriculumAlreadyExistsException() {
            super(String.format("ClassCurriculum already exists"));
        }
    }

    public static class CurriculumNotFoundException extends Exception {
        public CurriculumNotFoundException(final String message) {
            super(message);
        }
    }
}
