/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.schedule.load;

import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.page.ResponsePageableList;
import com.eshabakhov.schoodule.school.SchoolClass;
import com.eshabakhov.schoodule.school.Subject;
import com.eshabakhov.schoodule.tables.ClassCurriculum;
import com.eshabakhov.schoodule.tables.records.ClassCurriculumRecord;
import lombok.EqualsAndHashCode;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

/**
 * Postgres implementation of {@link ClassLoads}.
 *
 * @since 0.0.1
 * @checkstyle LambdaBodyLengthCheck (1000 lines)
 */
@EqualsAndHashCode
public final class ClassLoadsPostgres implements ClassLoads {

    /** JOOQ table for class loads. */
    private static final ClassCurriculum LOAD =
        ClassCurriculum.CLASS_CURRICULUM;

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

    /**
     * New class loads collection.
     *
     * @param ctx Database context
     * @param sid Schedule ID
     * @since 0.0.1
     */
    public ClassLoadsPostgres(final DSLContext ctx, final Long sid) {
        this.ctx = ctx;
        this.sid = sid;
    }

    @Override
    public ClassLoad create(
        final SchoolClass clazz,
        final Subject subject,
        final Integer hours
    ) throws Exception {
        return this.ctx.transactionResult(
            config -> {
                final DSLContext ttx = DSL.using(config);
                if (ttx.selectFrom(ClassLoadsPostgres.LOAD).where(
                    ClassLoadsPostgres.LOAD.SCHEDULE_ID.eq(this.sid)
                        .and(ClassLoadsPostgres.LOAD.SCHOOL_CLASS_ID.eq(clazz.uid()))
                        .and(ClassLoadsPostgres.LOAD.SUBJECT_ID.eq(subject.uid()))
                    )
                    .fetchOne() == null) {
                    final ClassCurriculumRecord created = ttx
                        .insertInto(ClassLoadsPostgres.LOAD)
                        .set(ClassLoadsPostgres.LOAD.SCHEDULE_ID, this.sid)
                        .set(ClassLoadsPostgres.LOAD.SCHOOL_CLASS_ID, clazz.uid())
                        .set(ClassLoadsPostgres.LOAD.SUBJECT_ID, subject.uid())
                        .set(ClassLoadsPostgres.LOAD.HOURS_PER_WEEK, hours)
                        .returning()
                        .fetchOne();
                    if (created == null) {
                        throw new LoadFailedCreateException();
                    }
                    return new ClassLoadPostgres(this.ctx, created.getId());
                } else {
                    throw new LoadAlreadyExistsException();
                }
            }
        );
    }

    @Override
    public ClassLoad load(final long lid) throws Exception {
        final ClassCurriculumRecord selected = this.ctx.selectFrom(
            ClassLoadsPostgres.LOAD
        ).where(
            ClassLoadsPostgres.LOAD.ID.eq(lid)
                .and(ClassLoadsPostgres.LOAD.SCHEDULE_ID.eq(this.sid))
            )
            .fetchOne();
        if (selected == null) {
            throw new LoadNotFoundException(
                String.format("Class load with id=%d not found", lid)
            );
        }
        return new ClassLoadPostgres(this.ctx, selected.getId());
    }

    @Override
    public PageableList<ClassLoad> list(
        final Condition condition,
        final Page page
    ) throws Exception {
        return new ResponsePageableList<>(
            this.ctx.select(
                ClassLoadsPostgres.LOAD.ID,
                ClassLoadsPostgres.CLASS.ID,
                ClassLoadsPostgres.SUBJECT.ID,
                ClassLoadsPostgres.LOAD.HOURS_PER_WEEK
                )
                .from(ClassLoadsPostgres.LOAD)
                .join(ClassLoadsPostgres.CLASS).on(
                    ClassLoadsPostgres.LOAD.SCHOOL_CLASS_ID.eq(
                        ClassLoadsPostgres.CLASS.ID
                    )
                )
                .join(ClassLoadsPostgres.SUBJECT).on(
                    ClassLoadsPostgres.LOAD.SUBJECT_ID.eq(
                        ClassLoadsPostgres.SUBJECT.ID
                    )
                )
                .where(ClassLoadsPostgres.LOAD.SCHEDULE_ID.eq(this.sid)).fetch(
                    selected ->
                        new ClassLoadPostgres(
                            this.ctx,
                            selected.get(ClassLoadsPostgres.LOAD.ID)
                        )
                ),
            this.ctx.fetchCount(
                this.ctx.selectFrom(ClassLoadsPostgres.LOAD)
                    .where(ClassLoadsPostgres.LOAD.SCHEDULE_ID.eq(this.sid))
            ),
            page
        );
    }

    @Override
    public void remove(final long lid) throws Exception {
        final ClassCurriculumRecord load = this.ctx.selectFrom(
            ClassLoadsPostgres.LOAD
        ).where(
            ClassLoadsPostgres.LOAD.ID.eq(lid)
                .and(ClassLoadsPostgres.LOAD.SCHEDULE_ID.eq(this.sid))
            )
            .fetchOne();
        if (load == null) {
            throw new LoadNotFoundException(
                String.format("Class load with id=%d not found", lid)
            );
        }
        this.ctx.transactionResult(
            config ->
                DSL.using(config).deleteFrom(ClassLoadsPostgres.LOAD)
                    .where(ClassLoadsPostgres.LOAD.ID.eq(lid))
                    .execute()
        );
    }
}
