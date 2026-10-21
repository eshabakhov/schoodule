/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.teacher;

import com.eshabakhov.surok.Page;
import com.eshabakhov.surok.PageableList;
import com.eshabakhov.surok.page.ResponsePageableList;
import com.eshabakhov.surok.school.Teacher;
import com.eshabakhov.surok.school.Teachers;
import com.eshabakhov.surok.sql.ClasspathSql;
import lombok.EqualsAndHashCode;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;

/**
 * Postgres implementation of {@link Teachers}.
 *
 * @since 0.0.1
 * @checkstyle LambdaBodyLengthCheck (1000 lines)
 */
@EqualsAndHashCode
public final class ThsPostgres implements Teachers {

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
                if (ttx.resultQuery(
                    new ClasspathSql("teacher/find-existing.sql").text(),
                    this.sid,
                    name,
                    false
                ).fetchOne() == null) {
                    final Record created = ttx.resultQuery(
                        new ClasspathSql("teacher/create.sql").text(),
                        this.sid,
                        name,
                        false
                    ).fetchOne();
                    if (created == null) {
                        throw new TeacherFailedCreateException();
                    }
                    return new ThPostgres(this.ctx, created.get("id", Long.class));
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
        final Record selected = this.ctx.resultQuery(
            new ClasspathSql("teacher/find-by-id-and-school-id.sql").text(),
            tid,
            this.sid,
            false
        ).fetchOne();
        if (selected == null) {
            throw new TeacherNotFoundException(
                String.format("Teacher with id=%d not found", tid)
            );
        }
        return new ThPostgres(this.ctx, selected.get("id", Long.class));
    }

    @Override
    public Teacher teacher(final String name) throws Exception {
        final Record selected = this.ctx.resultQuery(
            new ClasspathSql("teacher/find-existing.sql").text(),
            this.sid,
            name,
            false
        ).fetchOne();
        if (selected == null) {
            throw new TeacherNotFoundException(
                String.format("Teacher with name='%s' not found", name)
            );
        }
        return new ThPostgres(this.ctx, selected.get("id", Long.class));
    }

    @Override
    public PageableList<Teacher> teachers(final Condition condition, final Page page)
        throws Exception {
        return new ResponsePageableList<>(
            this.ctx.resultQuery(
                new ClasspathSql("teacher/list.sql").text(),
                condition.and(
                    DSL.condition(
                        new ClasspathSql("teacher/conditions/by-school-id.sql").text(),
                        this.sid
                    )
                ),
                DSL.val(page.limit()),
                DSL.val((page.offset() - 1) * page.limit())
            ).fetch(
                selected -> new ThPostgres(this.ctx, selected.get("id", Long.class))
            ),
            this.ctx.resultQuery(
                new ClasspathSql("teacher/count.sql").text(),
                condition
            ).fetchSingle(0, Integer.class),
            page
        );
    }

    @Override
    public void remove(final long tid) throws Exception {
        final Record selected = this.ctx.resultQuery(
            new ClasspathSql("teacher/find-by-id-and-school-id.sql").text(),
            tid,
            this.sid,
            false
        ).fetchOne();
        if (selected == null) {
            throw new TeacherNotFoundException(
                String.format("Teacher with id=%d not found", tid)
            );
        }
        this.ctx.transactionResult(
            config ->
                DSL.using(config).resultQuery(
                    new ClasspathSql("teacher/soft-delete.sql").text(),
                    true,
                    tid
                ).execute()
        );
    }
}
