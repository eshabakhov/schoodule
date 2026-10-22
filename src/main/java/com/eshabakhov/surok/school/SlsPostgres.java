/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school;

import com.eshabakhov.surok.Filters;
import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.Page;
import com.eshabakhov.surok.PrItems;
import com.eshabakhov.surok.School;
import com.eshabakhov.surok.Schools;
import com.eshabakhov.surok.Sorts;
import com.eshabakhov.surok.page.ResponsePage;
import com.eshabakhov.surok.school.filter.FlsCdSchool;
import com.eshabakhov.surok.school.input.MdSlInput;
import com.eshabakhov.surok.school.sort.StsJqSchool;
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
    public School create(final SchoolInput input) throws Exception {
        final String name = input.print(new MdSlInput()).print().get("name");
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
    public Schools selection(
        final Filters filters,
        final Page page,
        final Sorts sorts
    ) throws Exception {
        final Condition scoped = new FlsCdSchool(filters).condition().and(
            DSL.condition(
                new ClasspathSql("school/conditions/by-is-deleted.sql").text(),
                false
            )
        );
        return new SlsSelected(
            this,
            this.ctx.resultQuery(
                new ClasspathSql("school/list.sql").text(),
                scoped,
                DSL.list(new StsJqSchool(sorts).fields()),
                DSL.val(page.limit()),
                DSL.val((page.offset() - 1) * page.limit())
            ).fetch(
                selected -> new SlPostgres(
                    this.ctx,
                    selected.get("id", Long.class)
                )
            ),
            new ResponsePage(
                page,
                this.ctx.resultQuery(
                    new ClasspathSql("school/count.sql").text(),
                    scoped
                ).fetchSingle(0, Integer.class)
            )
        );
    }

    @Override
    public Iterable<School> iterate() {
        return this.ctx.resultQuery(
            new ClasspathSql("school/list-active.sql").text(),
            false
        ).fetch(selected -> new SlPostgres(this.ctx, selected.get("id", Long.class)));
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return media.with(new PrItems("school", this.iterate()));
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
}
