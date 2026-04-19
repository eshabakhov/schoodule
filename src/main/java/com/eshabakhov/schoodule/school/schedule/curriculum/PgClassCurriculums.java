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
import com.eshabakhov.schoodule.school.schoolclass.ScPostgres;
import com.eshabakhov.schoodule.school.subject.SbPostgres;
import com.eshabakhov.schoodule.tables.records.ClassCurriculumRecord;
import lombok.EqualsAndHashCode;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;

/**
 * Postgres implementation of {@link ClassCurriculums}.
 *
 * @since 0.0.1
 */
@EqualsAndHashCode
public final class PgClassCurriculums implements ClassCurriculums {

    /** JOOQ Table for ClassCurriculum. */
    private static final com.eshabakhov.schoodule.tables.ClassCurriculum CURRICULUM =
        com.eshabakhov.schoodule.tables.ClassCurriculum.CLASS_CURRICULUM;

    /** JOOQ School class table. */
    private static final com.eshabakhov.schoodule.tables.SchoolClass CLASS =
        com.eshabakhov.schoodule.tables.SchoolClass.SCHOOL_CLASS;

    /** JOOQ Subject table. */
    private static final com.eshabakhov.schoodule.tables.Subject SUBJECT =
        com.eshabakhov.schoodule.tables.Subject.SUBJECT;

    /** JOOQ Lesson assignment table. */
    private static final com.eshabakhov.schoodule.tables.LessonAssignment ASSIGNMENT =
        com.eshabakhov.schoodule.tables.LessonAssignment.LESSON_ASSIGNMENT;

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext datasource;

    /** Schedule ID. */
    private final Long sid;

    public PgClassCurriculums(final DSLContext datasource, final Long sid) {
        this.datasource = datasource;
        this.sid = sid;
    }

    @Override
    public ClassCurriculum add(
        final SchoolClass clazz,
        final Subject subject,
        final Integer hours
    ) throws Exception {
        return this.datasource.transactionResult(
            config -> {
                final DSLContext ctx = DSL.using(config);
                final var select = ctx.selectFrom(PgClassCurriculums.CURRICULUM)
                    .where(
                        PgClassCurriculums.CURRICULUM.SCHEDULE_ID.eq(this.sid)
                            .and(
                                PgClassCurriculums.CURRICULUM.SCHOOL_CLASS_ID.eq(
                                    clazz.uid()
                                )
                            )
                            .and(
                                PgClassCurriculums.CURRICULUM.SUBJECT_ID.eq(
                                    subject.uid()
                                )
                            )
                    )
                    .fetchOne();
                if (select == null) {
                    final var created = ctx.insertInto(PgClassCurriculums.CURRICULUM)
                        .set(PgClassCurriculums.CURRICULUM.SCHEDULE_ID, this.sid)
                        .set(
                            PgClassCurriculums.CURRICULUM.SCHOOL_CLASS_ID,
                            clazz.uid()
                        )
                        .set(PgClassCurriculums.CURRICULUM.SUBJECT_ID, subject.uid())
                        .set(
                            PgClassCurriculums.CURRICULUM.HOURS_PER_WEEK,
                            hours
                        )
                        .returning()
                        .fetchOne();
                    if (created == null) {
                        throw new CurriculumFailedCreateException();
                    }
                    return new SimpleClassCurriculum(
                        created.getId(),
                        new ScPostgres(this.datasource, created.getSchoolClassId()),
                        new SbPostgres(this.datasource, created.getSubjectId()),
                        created.getHoursPerWeek()
                    );
                } else {
                    throw new CurriculumAlreadyExistsException();
                }
            }
        );
    }

    @Override
    public ClassCurriculum find(final long cid) throws Exception {
        final var selected = this.datasource.selectFrom(PgClassCurriculums.CURRICULUM)
            .where(
                PgClassCurriculums.CURRICULUM.ID.eq(cid)
                    .and(PgClassCurriculums.CURRICULUM.SCHEDULE_ID.eq(this.sid))
            )
            .fetchOne();
        if (selected == null) {
            throw new CurriculumNotFoundException(
                String.format("ClassCurriculum with id=%d not found", cid)
            );
        }
        return new SimpleClassCurriculum(
            selected.getId(),
            new ScPostgres(this.datasource, selected.getSchoolClassId()),
            new SbPostgres(this.datasource, selected.getSubjectId()),
            selected.getHoursPerWeek()
        );
    }

    @Override
    public PageableList<ClassCurriculum> list(
        final Condition condition,
        final Page page
    ) throws Exception {
        final Field<Integer> assigned = PgClassCurriculums.ASSIGNMENT
            .HOURS_PER_WEEK.sum().cast(Integer.class).as("assigned_hours");
        return new ResponsePageableList<>(
            this.datasource
                .select(
                    PgClassCurriculums.CURRICULUM.ID,
                    PgClassCurriculums.CLASS.ID,
                    PgClassCurriculums.SUBJECT.ID,
                    PgClassCurriculums.CURRICULUM.HOURS_PER_WEEK,
                    assigned
                )
                .from(PgClassCurriculums.CURRICULUM)
                .join(PgClassCurriculums.CLASS)
                .on(
                    PgClassCurriculums.CURRICULUM.SCHOOL_CLASS_ID.eq(
                        PgClassCurriculums.CLASS.ID
                    )
                )
                .join(PgClassCurriculums.SUBJECT)
                .on(
                    PgClassCurriculums.CURRICULUM.SUBJECT_ID.eq(
                        PgClassCurriculums.SUBJECT.ID
                    )
                )
                .leftJoin(PgClassCurriculums.ASSIGNMENT)
                .on(
                    PgClassCurriculums.ASSIGNMENT.CLASS_CURRICULUM_ID.eq(
                        PgClassCurriculums.CURRICULUM.ID
                    )
                )
                .where(PgClassCurriculums.CURRICULUM.SCHEDULE_ID.eq(this.sid))
                .groupBy(
                    PgClassCurriculums.CURRICULUM.ID,
                    PgClassCurriculums.CLASS.ID,
                    PgClassCurriculums.SUBJECT.ID,
                    PgClassCurriculums.CURRICULUM.HOURS_PER_WEEK
                )
                .fetch(
                    selected -> new SimpleAssignedClassCurriculum(
                        new SimpleClassCurriculum(
                            selected.get(PgClassCurriculums.CURRICULUM.ID),
                            new ScPostgres(
                                this.datasource,
                                selected.get(PgClassCurriculums.CLASS.ID)
                            ),
                            new SbPostgres(
                                this.datasource,
                                selected.get(PgClassCurriculums.SUBJECT.ID)
                            ),
                            selected.get(PgClassCurriculums.CURRICULUM.HOURS_PER_WEEK)
                        ),
                        selected.get(assigned)
                    )
                ),
            this.datasource
                .fetchCount(
                    this.datasource.select(PgClassCurriculums.CURRICULUM.ID)
                        .from(PgClassCurriculums.CURRICULUM)
                        .join(PgClassCurriculums.CLASS)
                        .on(
                            PgClassCurriculums.CURRICULUM.SCHOOL_CLASS_ID.eq(
                                PgClassCurriculums.CLASS.ID
                            )
                        )
                        .join(PgClassCurriculums.SUBJECT)
                        .on(
                            PgClassCurriculums.CURRICULUM.SUBJECT_ID.eq(
                                PgClassCurriculums.SUBJECT.ID
                            )
                        )
                        .leftJoin(PgClassCurriculums.ASSIGNMENT)
                        .on(
                            PgClassCurriculums.ASSIGNMENT.CLASS_CURRICULUM_ID.eq(
                                PgClassCurriculums.CURRICULUM.ID
                            )
                        )
                        .where(PgClassCurriculums.CURRICULUM.SCHEDULE_ID.eq(this.sid))
                        .groupBy(
                            PgClassCurriculums.CURRICULUM.ID,
                            PgClassCurriculums.CLASS.ID,
                            PgClassCurriculums.SUBJECT.ID,
                            PgClassCurriculums.CURRICULUM.HOURS_PER_WEEK
                        )
                ),
            page
        );
    }

    @Override
    public ClassCurriculum put(
        final Long cid,
        final SchoolClass clazz,
        final Subject subject,
        final Integer hours
    ) throws Exception {
        final var select = this.datasource.selectFrom(PgClassCurriculums.CURRICULUM)
            .where(
                PgClassCurriculums.CURRICULUM.ID.eq(cid)
                    .and(PgClassCurriculums.CURRICULUM.SCHEDULE_ID.eq(this.sid))
            )
            .fetchOne();
        final ClassCurriculum result;
        if (select == null) {
            final var inserted = this.datasource.insertInto(PgClassCurriculums.CURRICULUM)
                .set(PgClassCurriculums.CURRICULUM.SCHEDULE_ID, this.sid)
                .set(
                    PgClassCurriculums.CURRICULUM.SCHOOL_CLASS_ID,
                    clazz.uid()
                )
                .set(PgClassCurriculums.CURRICULUM.SUBJECT_ID, subject.uid())
                .set(PgClassCurriculums.CURRICULUM.HOURS_PER_WEEK, hours)
                .returning()
                .fetchOne();
            if (inserted == null) {
                throw new CurriculumFailedCreateException();
            }
            result = new SimpleClassCurriculum(
                inserted.getId(),
                new ScPostgres(this.datasource, inserted.getSchoolClassId()),
                new SbPostgres(this.datasource, inserted.getSubjectId()),
                inserted.getHoursPerWeek()
            );
        } else {
            final var updated = this.datasource.update(PgClassCurriculums.CURRICULUM)
                .set(PgClassCurriculums.CURRICULUM.HOURS_PER_WEEK, hours)
                .where(PgClassCurriculums.CURRICULUM.ID.eq(cid))
                .returning()
                .fetchOne();
            if (updated == null) {
                throw new CurriculumFailedUpdateException();
            }
            result = new SimpleClassCurriculum(
                updated.getId(),
                new ScPostgres(this.datasource, updated.getSchoolClassId()),
                new SbPostgres(this.datasource, updated.getSubjectId()),
                updated.getHoursPerWeek()
            );
        }
        return result;
    }

    @Override
    public void remove(final long cid) throws Exception {
        final ClassCurriculumRecord curriculum = this.datasource
            .selectFrom(PgClassCurriculums.CURRICULUM)
            .where(
                PgClassCurriculums.CURRICULUM.ID.eq(cid)
                    .and(PgClassCurriculums.CURRICULUM.SCHEDULE_ID.eq(this.sid))
            )
            .fetchOne();
        if (curriculum == null) {
            throw new CurriculumNotFoundException(
                String.format("ClassCurriculum with id=%d not found", cid)
            );
        }
        this.datasource.transactionResult(
            config ->
                DSL.using(config).deleteFrom(PgClassCurriculums.CURRICULUM)
                    .where(PgClassCurriculums.CURRICULUM.ID.eq(cid))
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

    public static class CurriculumFailedUpdateException extends Exception {
        public CurriculumFailedUpdateException() {
            super("Failed to update ClassCurriculum");
        }
    }

    public static class CurriculumNotFoundException extends Exception {
        public CurriculumNotFoundException(final String message) {
            super(message);
        }
    }
}
