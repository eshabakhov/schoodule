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
 * @checkstyle LambdaBodyLengthCheck (1000 lines)
 */
@EqualsAndHashCode
public final class ThsPostgres implements Teachers {

    /** JOOQ Table for Teacher. */
    private static final com.eshabakhov.schoodule.tables.Teacher TEACHER =
        com.eshabakhov.schoodule.tables.Teacher.TEACHER;

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    /** School ID. */
    private final Long sid;

    /**
     * New teachers collection.
     *
     * @param ctx Database context
     * @param sid School ID
     * @since 0.0.1
     */
    public ThsPostgres(final DSLContext ctx, final Long sid) {
        this.ctx = ctx;
        this.sid = sid;
    }

    @Override
    public Teacher create(final String name) {
        return this.ctx.transactionResult(
            config -> {
                final DSLContext ttx = DSL.using(config);
                if (ttx.selectFrom(ThsPostgres.TEACHER).where(
                    ThsPostgres.TEACHER.SCHOOL_ID.eq(this.sid).and(
                        ThsPostgres.TEACHER.NAME.eq(name)
                    ).and(ThsPostgres.TEACHER.IS_DELETED.eq(false))
                    )
                    .fetchOne() == null) {
                    final TeacherRecord created = ttx.insertInto(ThsPostgres.TEACHER)
                        .set(ThsPostgres.TEACHER.SCHOOL_ID, this.sid)
                        .set(ThsPostgres.TEACHER.NAME, name)
                        .set(ThsPostgres.TEACHER.IS_DELETED, false)
                        .returning()
                        .fetchOne();
                    if (created == null) {
                        throw new TeacherFailedCreateException();
                    }
                    return new ThPostgres(this.ctx, created.getId());
                } else {
                    throw new TeacherAlreadyExistsException(
                        String.format("Teacher `%s` already exists", name)
                    );
                }
            }
        );
    }

    @Override
    public Teacher teacher(final long tid) throws Exception {
        final TeacherRecord selected = this.ctx.selectFrom(ThsPostgres.TEACHER).where(
            ThsPostgres.TEACHER.ID.eq(tid).and(
                ThsPostgres.TEACHER.SCHOOL_ID.eq(this.sid)
            ).and(ThsPostgres.TEACHER.IS_DELETED.eq(false))
            )
            .fetchOne();
        if (selected == null) {
            throw new TeacherNotFoundException(
                String.format("Teacher with id=%d not found", tid)
            );
        }
        return new ThPostgres(this.ctx, selected.getId());
    }

    @Override
    public Teacher teacher(final String name) throws Exception {
        final TeacherRecord selected = this.ctx.selectFrom(ThsPostgres.TEACHER).where(
            ThsPostgres.TEACHER.SCHOOL_ID.eq(this.sid).and(
                ThsPostgres.TEACHER.NAME.eq(name)
            ).and(ThsPostgres.TEACHER.IS_DELETED.eq(false))
            )
            .fetchOne();
        if (selected == null) {
            throw new TeacherNotFoundException(
                String.format("Teacher with name='%s' not found", name)
            );
        }
        return new ThPostgres(this.ctx, selected.getId());
    }

    @Override
    public PageableList<Teacher> teachers(final Condition condition, final Page page)
        throws Exception {
        return new ResponsePageableList<>(
            this.ctx.selectFrom(ThsPostgres.TEACHER)
                .where(condition.and(ThsPostgres.TEACHER.SCHOOL_ID.eq(this.sid)))
                .orderBy(ThsPostgres.TEACHER.NAME.asc())
                .limit(page.limit())
                .offset((page.offset() - 1) * page.limit()).fetch(
                    selected -> new ThPostgres(this.ctx, selected.getId())
                ),
            this.ctx.fetchCount(
                this.ctx.selectFrom(ThsPostgres.TEACHER).where(condition)
            ),
            page
        );
    }

    @Override
    public void remove(final long tid) throws Exception {
        final TeacherRecord selected = this.ctx.selectFrom(ThsPostgres.TEACHER).where(
            ThsPostgres.TEACHER.ID.eq(tid).and(
                ThsPostgres.TEACHER.SCHOOL_ID.eq(this.sid)
            ).and(ThsPostgres.TEACHER.IS_DELETED.eq(false))
            )
            .fetchOne();
        if (selected == null) {
            throw new TeacherNotFoundException(
                String.format("Teacher with id=%d not found", tid)
            );
        }
        this.ctx.transactionResult(
            config ->
                DSL.using(config).update(ThsPostgres.TEACHER)
                    .set(ThsPostgres.TEACHER.IS_DELETED, true)
                    .where(ThsPostgres.TEACHER.ID.eq(tid))
                    .execute()
        );
    }
}
