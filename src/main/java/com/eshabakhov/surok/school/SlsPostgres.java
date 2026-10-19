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
import com.eshabakhov.surok.tables.records.SchoolRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

/**
 * Postgres implementation of {@link Schools}.
 *
 * @since 0.0.1
 */
public final class SlsPostgres implements Schools {

    /** JOOQ Table for School. */
    private static final com.eshabakhov.surok.tables.School SCHOOL =
        com.eshabakhov.surok.tables.School.SCHOOL;

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
                final SchoolRecord created = DSL.using(config).insertInto(SlsPostgres.SCHOOL)
                    .set(SlsPostgres.SCHOOL.NAME, name)
                    .set(SlsPostgres.SCHOOL.IS_DELETED, false)
                    .returning()
                    .fetchOne();
                if (created == null) {
                    throw new SchoolFailedCreateException();
                }
                return new SlPostgres(this.ctx, created.getId());
            }
        );
    }

    @Override
    public School school(final long sid) throws Exception {
        final SchoolRecord selected = this.ctx.selectFrom(SlsPostgres.SCHOOL)
            .where(SlsPostgres.SCHOOL.ID.eq(sid).and(SlsPostgres.SCHOOL.IS_DELETED.eq(false)))
            .fetchOne();
        if (selected == null) {
            throw new SchoolNotFoundException(String.format("School with id=%d not found", sid));
        }
        return new SlPostgres(this.ctx, selected.getId());
    }

    @Override
    public School school(final String name) throws Exception {
        final SchoolRecord selected = this.ctx.selectFrom(SlsPostgres.SCHOOL)
            .where(SlsPostgres.SCHOOL.NAME.eq(name).and(SlsPostgres.SCHOOL.IS_DELETED.eq(false)))
            .fetchOne();
        if (selected == null) {
            throw new SchoolNotFoundException(
                String.format("School with name='%s' not found", name)
            );
        }
        return new SlPostgres(this.ctx, selected.getId());
    }

    @Override
    public PageableList<School> schools(final Condition condition, final Page page)
        throws Exception {
        return new ResponsePageableList<>(
            this.ctx.selectFrom(SlsPostgres.SCHOOL)
                .where(condition)
                .orderBy(SlsPostgres.SCHOOL.NAME.asc())
                .limit(page.limit())
                .offset((page.offset() - 1) * page.limit()).fetch(
                    selected -> new SlPostgres(this.ctx, selected.getId())
                ),
            this.ctx.fetchCount(
                this.ctx.selectFrom(SlsPostgres.SCHOOL).where(condition)
            ),
            page
        );
    }

    @Override
    public void remove(final long sid) throws Exception {
        final SchoolRecord selected = this.ctx.selectFrom(SlsPostgres.SCHOOL)
            .where(SlsPostgres.SCHOOL.ID.eq(sid).and(SlsPostgres.SCHOOL.IS_DELETED.eq(false)))
            .fetchOne();
        if (selected == null) {
            throw new SchoolNotFoundException(String.format("School with id=%d not found", sid));
        }
        this.ctx.transaction(
            config ->
                DSL.using(config).update(SlsPostgres.SCHOOL)
                    .set(SlsPostgres.SCHOOL.IS_DELETED, true)
                    .where(SlsPostgres.SCHOOL.ID.eq(sid))
                    .execute()
        );
    }

    @Override
    public <T> Media<T> print(final Media<T> media) {
        return media.with("href", "/schools");
    }
}
