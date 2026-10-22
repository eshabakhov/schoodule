/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.Page;
import com.eshabakhov.surok.PageableList;
import com.eshabakhov.surok.School;
import com.eshabakhov.surok.Schools;
import com.eshabakhov.surok.page.ResponsePageableList;
import com.eshabakhov.surok.sql.ClasspathSql;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;

/**
 * Postgres implementation of {@link Schools}.
 *
 * @since 0.0.1
 */
public final class SlsPostgres implements Schools {

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    /**
     * New schools collection.
     *
     * @param ctx Database context
     * @since 0.0.1
     */
    public SlsPostgres(final DSLContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public School create(final String name) throws Exception {
        return this.ctx.transactionResult(
            config -> {
                final Record created = DSL.using(config).resultQuery(
                    new ClasspathSql("school/create.sql").text(),
                    name,
                    false
                ).fetchOne();
                if (created == null) {
                    throw new SchoolFailedCreateException();
                }
                return new SlPostgres(this.ctx, created.get("id", Long.class));
            }
        );
    }

    @Override
    public School school(final long sid) throws Exception {
        final Record selected = this.ctx.resultQuery(
            new ClasspathSql("school/find-by-id.sql").text(),
            sid,
            false
        ).fetchOne();
        if (selected == null) {
            throw new SchoolNotFoundException(String.format("School with id=%d not found", sid));
        }
        return new SlPostgres(this.ctx, selected.get("id", Long.class));
    }

    @Override
    public School school(final String name) throws Exception {
        final Record selected = this.ctx.resultQuery(
            new ClasspathSql("school/find-by-name.sql").text(),
            name,
            false
        ).fetchOne();
        if (selected == null) {
            throw new SchoolNotFoundException(
                String.format("School with name='%s' not found", name)
            );
        }
        return new SlPostgres(this.ctx, selected.get("id", Long.class));
    }

    @Override
    public PageableList<School> schools(final Condition condition, final Page page)
        throws Exception {
        return new ResponsePageableList<>(
            this.ctx.resultQuery(
                new ClasspathSql("school/list.sql").text(),
                condition,
                DSL.val(page.limit()),
                DSL.val((page.offset() - 1) * page.limit())
            ).fetch(
                selected -> new SlPostgres(this.ctx, selected.get("id", Long.class))
            ),
            this.ctx.resultQuery(
                new ClasspathSql("school/count.sql").text(),
                condition
            ).fetchSingle(0, Integer.class),
            page
        );
    }

    @Override
    public void remove(final long sid) throws Exception {
        final Record selected = this.ctx.resultQuery(
            new ClasspathSql("school/find-by-id.sql").text(),
            sid,
            false
        ).fetchOne();
        if (selected == null) {
            throw new SchoolNotFoundException(String.format("School with id=%d not found", sid));
        }
        this.ctx.transaction(
            config ->
                DSL.using(config).resultQuery(
                    new ClasspathSql("school/soft-delete.sql").text(),
                    true,
                    sid
                ).execute()
        );
    }

    @Override
    public <T> Media<T> print(final Media<T> media) {
        return media.with("href", "/schools");
    }
}
