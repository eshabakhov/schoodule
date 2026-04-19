/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.teacher;

import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.page.ResponsePageableList;
import com.eshabakhov.schoodule.school.Teacher;
import com.eshabakhov.schoodule.school.Teachers;
import com.eshabakhov.schoodule.tables.records.TeacherRecord;
import lombok.EqualsAndHashCode;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

/**
 * Postgres implementation of {@link Teachers}.
 *
 * @since 0.0.1
 */
@EqualsAndHashCode
public final class ThsPostgres implements Teachers {

    /** JOOQ Table for Teacher. */
    private static final com.eshabakhov.schoodule.tables.Teacher TEACHER =
        com.eshabakhov.schoodule.tables.Teacher.TEACHER;

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext datasource;

    /** School ID. */
    private final Long sid;

    public ThsPostgres(final DSLContext datasource, final Long sid) {
        this.datasource = datasource;
        this.sid = sid;
    }

    @Override
    public Teacher create(final Teacher teacher) {
        return this.datasource.transactionResult(
            config -> {
                final DSLContext ctx = DSL.using(config);
                final var rec = ctx.selectFrom(ThsPostgres.TEACHER)
                    .where(
                        ThsPostgres.TEACHER.SCHOOL_ID.eq(this.sid)
                            .and(ThsPostgres.TEACHER.NAME.eq(teacher.name()))
                            .and(ThsPostgres.TEACHER.IS_DELETED.eq(false))
                    )
                    .fetchOne();
                if (rec == null) {
                    final var created = ctx.insertInto(ThsPostgres.TEACHER)
                        .set(ThsPostgres.TEACHER.SCHOOL_ID, this.sid)
                        .set(ThsPostgres.TEACHER.NAME, teacher.name())
                        .set(ThsPostgres.TEACHER.IS_DELETED, false)
                        .returning()
                        .fetchOne();
                    if (created == null) {
                        throw new TeacherFailedCreateException();
                    }
                    return new ThPostgres(this.datasource, created.getId());
                } else {
                    throw new TeacherAlreadyExistsException(teacher);
                }
            }
        );
    }

    @Override
    public Teacher find(final long tid) throws Exception {
        final TeacherRecord selected = this.datasource.selectFrom(ThsPostgres.TEACHER)
            .where(
                ThsPostgres.TEACHER.ID.eq(tid)
                    .and(ThsPostgres.TEACHER.SCHOOL_ID.eq(this.sid))
                    .and(ThsPostgres.TEACHER.IS_DELETED.eq(false))
            )
            .fetchOne();
        if (selected == null) {
            throw new TeacherNotFoundException(
                String.format("Teacher with id=%d not found", tid)
            );
        }
        return new ThPostgres(this.datasource, selected.getId());
    }

    @Override
    public Teacher find(final String name) throws Exception {
        final TeacherRecord selected = this.datasource.selectFrom(ThsPostgres.TEACHER)
            .where(
                ThsPostgres.TEACHER.SCHOOL_ID.eq(this.sid)
                    .and(ThsPostgres.TEACHER.NAME.eq(name))
                    .and(ThsPostgres.TEACHER.IS_DELETED.eq(false))
            )
            .fetchOne();
        if (selected == null) {
            throw new TeacherNotFoundException(
                String.format("Teacher with name='%s' not found", name)
            );
        }
        return new ThPostgres(this.datasource, selected.getId());
    }

    @Override
    public PageableList<Teacher> list(final Condition condition, final Page page) throws Exception {
        return new ResponsePageableList<>(
            this.datasource.selectFrom(ThsPostgres.TEACHER)
                .where(condition.and(ThsPostgres.TEACHER.SCHOOL_ID.eq(this.sid)))
                .orderBy(ThsPostgres.TEACHER.NAME.asc())
                .limit(page.limit())
                .offset((page.offset() - 1) * page.limit())
                .fetch(
                    selected -> new ThPostgres(this.datasource, selected.getId())
                ),
            this.datasource.fetchCount(
                this.datasource.selectFrom(ThsPostgres.TEACHER).where(condition)
            ),
            page
        );
    }

    @Override
    public Teacher put(final Teacher teacher) throws Exception {
        final TeacherRecord selected = this.datasource.selectFrom(ThsPostgres.TEACHER)
            .where(
                ThsPostgres.TEACHER.ID.eq(teacher.uid())
                    .and(ThsPostgres.TEACHER.SCHOOL_ID.eq(this.sid))
                    .and(ThsPostgres.TEACHER.IS_DELETED.eq(false))
            )
            .fetchOne();
        final Teacher result;
        if (selected == null) {
            final TeacherRecord insert = this.datasource.insertInto(ThsPostgres.TEACHER)
                .set(ThsPostgres.TEACHER.SCHOOL_ID, this.sid)
                .set(ThsPostgres.TEACHER.NAME, teacher.name())
                .set(ThsPostgres.TEACHER.IS_DELETED, false)
                .returning()
                .fetchOne();
            if (insert == null) {
                throw new TeacherFailedCreateException();
            }
            result = new ThPostgres(this.datasource, selected.getId());
        } else {
            final TeacherRecord updated = this.datasource.update(ThsPostgres.TEACHER)
                .set(ThsPostgres.TEACHER.NAME, teacher.name())
                .where(ThsPostgres.TEACHER.ID.eq(teacher.uid()))
                .returning()
                .fetchOne();
            if (updated == null) {
                throw new TeacherFailedUpdateException();
            }
            result = new ThPostgres(this.datasource, updated.getId());
        }
        return result;
    }

    @Override
    public void remove(final long tid) throws Exception {
        final TeacherRecord selected = this.datasource.selectFrom(ThsPostgres.TEACHER)
            .where(
                ThsPostgres.TEACHER.ID.eq(tid)
                    .and(ThsPostgres.TEACHER.SCHOOL_ID.eq(this.sid))
                    .and(ThsPostgres.TEACHER.IS_DELETED.eq(false))
            )
            .fetchOne();
        if (selected == null) {
            throw new TeacherNotFoundException(
                String.format("Teacher with id=%d not found", tid)
            );
        }
        this.datasource.transactionResult(
            config ->
                DSL.using(config).update(ThsPostgres.TEACHER)
                    .set(ThsPostgres.TEACHER.IS_DELETED, true)
                    .where(ThsPostgres.TEACHER.ID.eq(tid))
                    .execute()
        );
    }

    public static class TeacherFailedCreateException extends Exception {
        public TeacherFailedCreateException() {
            super("Failed to create Teacher");
        }
    }

    public static class TeacherAlreadyExistsException extends Exception {
        public TeacherAlreadyExistsException(final Teacher teacher) {
            super(String.format("Teacher `%s` already exists", teacher.json()));
        }
    }

    public static class TeacherFailedUpdateException extends Exception {
        public TeacherFailedUpdateException() {
            super("Failed to update Teacher");
        }
    }

    public static class TeacherNotFoundException extends Exception {
        public TeacherNotFoundException(final String message) {
            super(message);
        }
    }
}
