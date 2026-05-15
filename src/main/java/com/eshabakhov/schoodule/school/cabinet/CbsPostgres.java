/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.cabinet;

import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.page.ResponsePageableList;
import com.eshabakhov.schoodule.school.Cabinet;
import com.eshabakhov.schoodule.school.Cabinets;
import com.eshabakhov.schoodule.tables.records.CabinetRecord;
import lombok.EqualsAndHashCode;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

/**
 * Postgres implementation of {@link Cabinets}.
 *
 * @since 0.0.1
 */
@EqualsAndHashCode
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public final class CbsPostgres implements Cabinets {

    /** JOOQ Table for Cabinet. */
    private static final com.eshabakhov.schoodule.tables.Cabinet CABINET =
        com.eshabakhov.schoodule.tables.Cabinet.CABINET;

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    /** School. */
    private final Long sid;

    public CbsPostgres(final DSLContext ctx, final Long sid) {
        this.ctx = ctx;
        this.sid = sid;
    }

    @Override
    public Cabinet add(final String name) throws Exception {
        return this.ctx.transactionResult(
            config -> {
                final DSLContext ttx = DSL.using(config);
                final var select = ttx.selectFrom(CbsPostgres.CABINET)
                    .where(
                        CbsPostgres.CABINET.SCHOOL_ID.eq(this.sid)
                            .and(CbsPostgres.CABINET.NAME.eq(name))
                            .and(CbsPostgres.CABINET.IS_DELETED.eq(false))
                    )
                    .fetchOne();
                if (select == null) {
                    final var created = ttx.insertInto(CbsPostgres.CABINET)
                        .set(CbsPostgres.CABINET.SCHOOL_ID, this.sid)
                        .set(CbsPostgres.CABINET.NAME, name)
                        .set(CbsPostgres.CABINET.IS_DELETED, false)
                        .returning()
                        .fetchOne();
                    if (created == null) {
                        throw new CabinetFailedCreateException();
                    }
                    return new CbPostgres(this.ctx, created.getId());
                } else {
                    throw new CabinetAlreadyExistsException(name);
                }
            }
        );
    }

    @Override
    public Cabinet cabinet(final long cid) throws Exception {
        final var selected = this.ctx.selectFrom(CbsPostgres.CABINET)
            .where(
                CbsPostgres.CABINET.ID.eq(cid)
                    .and(CbsPostgres.CABINET.SCHOOL_ID.eq(this.sid))
                    .and(CbsPostgres.CABINET.IS_DELETED.eq(false))
            )
            .fetchOne();
        if (selected == null) {
            throw new CabinetNotFoundException(
                String.format("Cabinet with id=%d not found", cid)
            );
        }
        return new CbPostgres(this.ctx, selected.getId());
    }

    @Override
    public Cabinet cabinet(final String name) throws Exception {
        final CabinetRecord selected = this.ctx.selectFrom(CbsPostgres.CABINET)
            .where(
                CbsPostgres.CABINET.SCHOOL_ID.eq(this.sid)
                    .and(CbsPostgres.CABINET.NAME.eq(name))
                    .and(CbsPostgres.CABINET.IS_DELETED.eq(false))
            )
            .fetchOne();
        if (selected == null) {
            throw new CabinetNotFoundException(
                String.format("Cabinet with name=`%s` not found", name)
            );
        }
        return new CbPostgres(this.ctx, selected.getId());
    }

    @Override
    public PageableList<Cabinet> cabinets(final Condition condition, final Page page)
        throws Exception {
        final var cnd = condition.and(CbsPostgres.CABINET.SCHOOL_ID.eq(this.sid));
        return new ResponsePageableList<>(
            this.ctx.selectFrom(CbsPostgres.CABINET)
                .where(cnd)
                .orderBy(CbsPostgres.CABINET.NAME.asc())
                .limit(page.limit())
                .offset((page.offset() - 1) * page.limit())
                .fetch(
                    selected -> new CbPostgres(this.ctx, selected.getId())
                ),
            this.ctx.fetchCount(
                this.ctx.selectFrom(CbsPostgres.CABINET).where(cnd)
            ),
            page
        );
    }

    @Override
    public Cabinet put(final Long cid, final String name) throws Exception {
        final var selected = this.ctx.selectFrom(CbsPostgres.CABINET)
            .where(
                CbsPostgres.CABINET.ID.eq(cid)
                    .and(CbsPostgres.CABINET.SCHOOL_ID.eq(this.sid))
                    .and(CbsPostgres.CABINET.IS_DELETED.eq(false))
            )
            .fetchOne();
        final Cabinet result;
        if (selected == null) {
            final var insert = this.ctx.insertInto(CbsPostgres.CABINET)
                .set(CbsPostgres.CABINET.SCHOOL_ID, this.sid)
                .set(CbsPostgres.CABINET.NAME, name)
                .set(CbsPostgres.CABINET.IS_DELETED, false)
                .returning()
                .fetchOne();
            if (insert == null) {
                throw new CabinetFailedCreateException();
            }
            result = new CbPostgres(this.ctx, insert.getId());
        } else {
            final var updated = this.ctx.update(CbsPostgres.CABINET)
                .set(CbsPostgres.CABINET.NAME, name)
                .where(CbsPostgres.CABINET.ID.eq(cid))
                .returning()
                .fetchOne();
            if (updated == null) {
                throw new CabinetFailedUpdateException();
            }
            result = new CbPostgres(this.ctx, updated.getId());
        }
        return result;
    }

    @Override
    public void remove(final long cid) throws Exception {
        final CabinetRecord cabinet = this.ctx.selectFrom(CbsPostgres.CABINET)
            .where(CbsPostgres.CABINET.ID.eq(cid)
                .and(CbsPostgres.CABINET.SCHOOL_ID.eq(this.sid))
                .and(CbsPostgres.CABINET.IS_DELETED.eq(false))
            )
            .fetchOne();
        if (cabinet == null) {
            throw new CabinetNotFoundException(String.format("Cabinet with id=%d not found", cid));
        }
        this.ctx.update(CbsPostgres.CABINET)
            .set(CbsPostgres.CABINET.IS_DELETED, true)
            .where(CbsPostgres.CABINET.ID.eq(cid))
            .execute();
    }

    public static class CabinetFailedCreateException extends Exception {
        public CabinetFailedCreateException() {
            super("Failed to create Cabinet");
        }
    }

    public static class CabinetAlreadyExistsException extends Exception {
        public CabinetAlreadyExistsException(final String name) {
            super(String.format("Cabinet `%s` already exists", name));
        }
    }

    public static class CabinetFailedUpdateException extends Exception {
        public CabinetFailedUpdateException() {
            super("Failed to update Subject");
        }
    }

    public static class CabinetNotFoundException extends Exception {
        public CabinetNotFoundException(final String message) {
            super(message);
        }
    }
}
