/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.subject;

import com.eshabakhov.surok.Page;
import com.eshabakhov.surok.PageableList;
import com.eshabakhov.surok.page.ResponsePageableList;
import com.eshabakhov.surok.school.Subject;
import com.eshabakhov.surok.school.Subjects;
import com.eshabakhov.surok.sql.ClasspathSql;
import lombok.EqualsAndHashCode;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;

/**
 * Postgres implementation of {@link Subjects}.
 *
 * @since 0.0.1
 * @checkstyle LambdaBodyLengthCheck (1000 lines)
 */
@EqualsAndHashCode
public final class SbsPostgres implements Subjects {

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    /** School ID. */
    private final Long sid;

    /**
     * New subjects collection.
     *
     * @param ctx Database context
     * @param sid School ID
     * @since 0.0.1
     */
    public SbsPostgres(final DSLContext ctx, final Long sid) {
        this.ctx = ctx;
        this.sid = sid;
    }

    @Override
    public Subject create(final String name) throws Exception {
        return this.ctx.transactionResult(
            config -> {
                final DSLContext ttx = DSL.using(config);
                if (ttx.resultQuery(
                    new ClasspathSql("subject/find-existing.sql").text(),
                    this.sid,
                    name,
                    false
                ).fetchOne() == null) {
                    final Record created = ttx.resultQuery(
                        new ClasspathSql("subject/create.sql").text(),
                        this.sid,
                        name,
                        false
                    ).fetchOne();
                    if (created == null) {
                        throw new SubjectFailedCreateException();
                    }
                    return new SbPostgres(this.ctx, created.get("id", Long.class));
                } else {
                    throw new SubjectAlreadyExistsException(
                        String.format("Subject `%s` already exists", name)
                    );
                }
            }
        );
    }

    @Override
    public Subject subject(final long subid) throws Exception {
        final Record selected = this.ctx.resultQuery(
            new ClasspathSql("subject/find-by-id-and-school-id.sql").text(),
            subid,
            this.sid,
            false
        ).fetchOne();
        if (selected == null) {
            throw new SubjectNotFoundException(
                String.format("Subject `%s` not found", subid)
            );
        }
        return new SbPostgres(this.ctx, selected.get("id", Long.class));
    }

    @Override
    public Subject subject(final String name) throws Exception {
        final Record selected = this.ctx.resultQuery(
            new ClasspathSql("subject/find-existing.sql").text(),
            this.sid,
            name,
            false
        ).fetchOne();
        if (selected == null) {
            throw new SubjectNotFoundException(
                String.format("Subject `%s` not found", name)
            );
        }
        return new SbPostgres(this.ctx, selected.get("id", Long.class));
    }

    @Override
    public PageableList<Subject> subjects(final Condition condition, final Page page)
        throws Exception {
        return new ResponsePageableList<>(
            this.ctx.resultQuery(
                new ClasspathSql("subject/list.sql").text(),
                condition.and(
                    DSL.condition(
                        new ClasspathSql("subject/conditions/by-school-id.sql").text(),
                        this.sid
                    )
                ),
                DSL.val(page.limit()),
                DSL.val((page.offset() - 1) * page.limit())
            ).fetch(selected -> new SbPostgres(this.ctx, selected.get("id", Long.class))),
            this.ctx.resultQuery(
                new ClasspathSql("subject/count.sql").text(),
                condition
            ).fetchSingle(0, Integer.class),
            page
        );
    }

    @Override
    public void remove(final long subid) throws Exception {
        final Record subject = this.ctx.resultQuery(
            new ClasspathSql("subject/find-by-id-and-school-id.sql").text(),
            subid,
            this.sid,
            false
        ).fetchOne();
        if (subject == null) {
            throw new SubjectNotFoundException(
                String.format("Subject with id=%d not found", subid)
            );
        }
        this.ctx.transactionResult(
            config ->
                DSL.using(config).resultQuery(
                    new ClasspathSql("subject/soft-delete.sql").text(),
                    true,
                    subid
                ).execute()
        );
    }
}
