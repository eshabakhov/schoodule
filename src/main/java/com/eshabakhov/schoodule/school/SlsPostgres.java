/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school;

import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.School;
import com.eshabakhov.schoodule.Schools;
import com.eshabakhov.schoodule.page.ResponsePageableList;
import com.eshabakhov.schoodule.tables.records.SchoolRecord;
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
    private static final com.eshabakhov.schoodule.tables.School SCHOOL =
        com.eshabakhov.schoodule.tables.School.SCHOOL;

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    public SlsPostgres(final DSLContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public School create(final String name) throws Exception {
        return this.ctx.transactionResult(
            config -> {
                final var created = DSL.using(config).insertInto(SlsPostgres.SCHOOL)
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
    public School find(final long sid) throws Exception {
        final SchoolRecord selected = this.ctx.selectFrom(SlsPostgres.SCHOOL)
            .where(SlsPostgres.SCHOOL.ID.eq(sid).and(SlsPostgres.SCHOOL.IS_DELETED.eq(false)))
            .fetchOne();
        if (selected == null) {
            throw new SchoolNotFoundException(String.format("School with id=%d not found", sid));
        }
        return new SlPostgres(this.ctx, selected.getId());
    }

    @Override
    public School find(final String name) throws Exception {
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
    public PageableList<School> list(final Condition condition, final Page page) throws Exception {
        return new ResponsePageableList<>(
            this.ctx.selectFrom(SlsPostgres.SCHOOL)
                .where(condition)
                .orderBy(SlsPostgres.SCHOOL.NAME.asc())
                .limit(page.limit())
                .offset((page.offset() - 1) * page.limit())
                .fetch(
                    selected -> new SlPostgres(this.ctx, selected.getId())
                ),
            this.ctx.fetchCount(
                this.ctx.selectFrom(SlsPostgres.SCHOOL).where(condition)
            ),
            page
        );
    }

    @Override
    public School put(final School school) throws Exception {
        final SchoolRecord select = this.ctx.selectFrom(SlsPostgres.SCHOOL)
            .where(
                SlsPostgres.SCHOOL.ID.eq(school.uid())
                    .and(SlsPostgres.SCHOOL.IS_DELETED.eq(false))
            )
            .fetchOne();
        final School result;
        if (select == null) {
            final SchoolRecord inserted = this.ctx.insertInto(SlsPostgres.SCHOOL)
                .set(SlsPostgres.SCHOOL.NAME, school.name())
                .set(SlsPostgres.SCHOOL.IS_DELETED, false)
                .returning()
                .fetchOne();
            if (inserted == null) {
                throw new SchoolFailedCreateException();
            }
            result = new SlPostgres(this.ctx, inserted.getId());
        } else {
            final SchoolRecord update = this.ctx.update(SlsPostgres.SCHOOL)
                .set(SlsPostgres.SCHOOL.NAME, school.name())
                .where(SlsPostgres.SCHOOL.ID.eq(school.uid()))
                .returning()
                .fetchOne();
            if (update == null) {
                throw new SchoolFailedUpdateException();
            }
            result = school;
        }
        return result;
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

    public static class SchoolFailedCreateException extends Exception {
        public SchoolFailedCreateException() {
            super("Failed to create School");
        }
    }

    public static class SchoolFailedUpdateException extends Exception {
        public SchoolFailedUpdateException() {
            super("Failed to update School");
        }
    }

    public static class SchoolNotFoundException extends Exception {
        public SchoolNotFoundException(final String message) {
            super(message);
        }
    }
}
