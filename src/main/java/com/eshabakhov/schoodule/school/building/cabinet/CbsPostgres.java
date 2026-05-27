/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.building.cabinet;

import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.page.ResponsePageableList;
import com.eshabakhov.schoodule.school.building.Cabinet;
import com.eshabakhov.schoodule.school.building.Cabinets;
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

    /** Building id. */
    private final Long bid;

    public CbsPostgres(final DSLContext ctx, final Long bid) {
        this.ctx = ctx;
        this.bid = bid;
    }

    @Override
    public Cabinet create(final String name) throws Exception {
        return this.ctx.transactionResult(
            config -> {
                final DSLContext ttx = DSL.using(config);
                final var select = ttx.selectFrom(CbsPostgres.CABINET)
                    .where(
                        CbsPostgres.CABINET.BUILDING_ID.eq(this.bid)
                            .and(CbsPostgres.CABINET.NAME.eq(name))
                            .and(CbsPostgres.CABINET.IS_DELETED.eq(false))
                    )
                    .fetchOne();
                if (select == null) {
                    final var created = ttx.insertInto(CbsPostgres.CABINET)
                        .set(CbsPostgres.CABINET.BUILDING_ID, this.bid)
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
                    .and(CbsPostgres.CABINET.BUILDING_ID.eq(this.bid))
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
                CbsPostgres.CABINET.BUILDING_ID.eq(this.bid)
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
        final var cnd = condition.and(CbsPostgres.CABINET.BUILDING_ID.eq(this.bid));
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
    public void remove(final long cid) throws Exception {
        final CabinetRecord cabinet = this.ctx.selectFrom(CbsPostgres.CABINET)
            .where(CbsPostgres.CABINET.ID.eq(cid)
                .and(CbsPostgres.CABINET.BUILDING_ID.eq(this.bid))
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

    public static class CabinetNotFoundException extends Exception {
        public CabinetNotFoundException(final String message) {
            super(message);
        }
    }
}
