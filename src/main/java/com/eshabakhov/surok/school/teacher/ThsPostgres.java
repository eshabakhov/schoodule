/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.teacher;

import com.eshabakhov.surok.Filters;
import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.Page;
import com.eshabakhov.surok.PrItems;
import com.eshabakhov.surok.Sorts;
import com.eshabakhov.surok.page.ResponsePage;
import com.eshabakhov.surok.school.teacher.filter.FlsCdTeacher;
import com.eshabakhov.surok.school.teacher.input.MdThInput;
import com.eshabakhov.surok.school.teacher.sort.StsJqTeacher;
import com.eshabakhov.surok.sql.ClasspathSql;
import lombok.EqualsAndHashCode;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

/**
 * Postgres implementation of {@link Teachers}.
 *
 * <p>Checked domain failures are unwrapped from transaction failures.</p>
 *
 * @since 0.0.1
 * @checkstyle LambdaBodyLengthCheck (1000 lines)
 */
@EqualsAndHashCode
public final class ThsPostgres implements Teachers {

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    /** School id. */
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
    @SuppressWarnings("PMD.PreserveStackTrace")
    public Teacher create(final TeacherInput input) throws Exception {
        final String name = input.print(new MdThInput()).print().get("name");
        try {
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
        } catch (final DataAccessException exception) {
            switch (exception.getCause()) {
                case TeacherAlreadyExistsException duplicate -> throw duplicate;
                case TeacherFailedCreateException failed -> throw failed;
                case null, default -> throw exception;
            }
        }
    }

    @Override
    public Teacher teacher(final long cid) throws Exception {
        final Record selected = this.ctx.resultQuery(
            new ClasspathSql("teacher/find-by-id-and-school-id.sql").text(),
            cid,
            this.sid,
            false
        ).fetchOne();
        if (selected == null) {
            throw new TeacherNotFoundException(
                String.format("Teacher with id=%d not found", cid)
            );
        }
        return new ThPostgres(this.ctx, selected.get("id", Long.class));
    }

    @Override
    public Teachers selection(
        final Filters filters,
        final Page page,
        final Sorts sorts
    ) throws Exception {
        final Condition scoped = new FlsCdTeacher(filters).condition().and(
            DSL.condition(
                new ClasspathSql("teacher/conditions/by-school-id-and-is-deleted.sql").text(),
                this.sid,
                false
            )
        );
        return new ThsSelected(
            this,
            this.ctx.resultQuery(
                new ClasspathSql("teacher/list.sql").text(),
                scoped,
                DSL.list(new StsJqTeacher(sorts).fields()),
                DSL.val(page.limit()),
                DSL.val((page.offset() - 1) * page.limit())
            ).fetch(selected -> new ThPostgres(this.ctx, selected.get("id", Long.class))),
            new ResponsePage(
                page,
                this.ctx.resultQuery(
                    new ClasspathSql("teacher/count.sql").text(), scoped
                ).fetchSingle(0, Integer.class)
            )
        );
    }

    @Override
    public Iterable<Teacher> iterate() {
        return this.ctx.resultQuery(
            new ClasspathSql("teacher/list-active.sql").text(), this.sid, false
        ).fetch(selected -> new ThPostgres(this.ctx, selected.get("id", Long.class)));
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return media.with(new PrItems("teacher", this.iterate()));
    }

    @Override
    public void remove(final long cid) throws Exception {
        if (this.ctx.resultQuery(
            new ClasspathSql("teacher/find-by-id-and-school-id.sql").text(),
            cid,
            this.sid,
            false
        ).fetchOne() == null) {
            throw new TeacherNotFoundException(
                String.format("Teacher with id=%d not found", cid)
            );
        }
        this.ctx.resultQuery(
            new ClasspathSql("teacher/soft-delete.sql").text(), true, cid
        ).execute();
    }
}
