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
public final class CbsPostgres implements Cabinets {

    /** JOOQ Table for Cabinet. */
    private static final com.eshabakhov.schoodule.tables.Cabinet CABINET =
        com.eshabakhov.schoodule.tables.Cabinet.CABINET;

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext datasource;

    /** School. */
    private final Long sid;

    public CbsPostgres(final DSLContext datasource, final Long sid) {
        this.datasource = datasource;
        this.sid = sid;
    }

    @Override
    public Cabinet add(final String name) throws Exception {
        return this.datasource.transactionResult(
            config -> {
                final DSLContext ctx = DSL.using(config);
                final var select = ctx.selectFrom(CbsPostgres.CABINET)
                    .where(
                        CbsPostgres.CABINET.SCHOOL_ID.eq(this.sid)
                            .and(CbsPostgres.CABINET.NAME.eq(name))
                            .and(CbsPostgres.CABINET.IS_DELETED.eq(false))
                    )
                    .fetchOne();
                if (select == null) {
                    final var created = ctx.insertInto(CbsPostgres.CABINET)
                        .set(CbsPostgres.CABINET.SCHOOL_ID, this.sid)
                        .set(CbsPostgres.CABINET.NAME, name)
                        .set(CbsPostgres.CABINET.IS_DELETED, false)
                        .returning()
                        .fetchOne();
                    if (created == null) {
                        throw new CabinetFailedCreateException();
                    }
                    return new CbPostgres(this.datasource, created.getId());
                } else {
                    throw new CabinetAlreadyExistsException(name);
                }
            }
        );
    }

    @Override
    public Cabinet find(final long cid) throws Exception {
        final var selected = this.datasource.selectFrom(CbsPostgres.CABINET)
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
        return new CbPostgres(this.datasource, selected.getId());
    }

    @Override
    public Cabinet find(final String name) throws Exception {
        final CabinetRecord selected = this.datasource.selectFrom(CbsPostgres.CABINET)
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
        return new CbPostgres(this.datasource, selected.getId());
    }

    @Override
    public PageableList<Cabinet> list(final Condition condition, final Page page) throws Exception {
        return new ResponsePageableList<>(
            this.datasource.selectFrom(CbsPostgres.CABINET)
                .where(condition.and(CbsPostgres.CABINET.SCHOOL_ID.eq(this.sid)))
                .orderBy(CbsPostgres.CABINET.NAME.asc())
                .limit(page.limit())
                .offset((page.offset() - 1) * page.limit())
                .fetch(
                    selected -> new CbPostgres(this.datasource, selected.getId())
                ),
            this.datasource.fetchCount(
                this.datasource.selectFrom(CbsPostgres.CABINET).where(condition)
            ),
            page
        );
    }

    @Override
    public Cabinet put(final Long cid, final String name) throws Exception {
        final var selected = this.datasource.selectFrom(CbsPostgres.CABINET)
            .where(
                CbsPostgres.CABINET.ID.eq(cid)
                    .and(CbsPostgres.CABINET.SCHOOL_ID.eq(this.sid))
                    .and(CbsPostgres.CABINET.IS_DELETED.eq(false))
            )
            .fetchOne();
        final Cabinet result;
        if (selected == null) {
            final var insert = this.datasource.insertInto(CbsPostgres.CABINET)
                .set(CbsPostgres.CABINET.SCHOOL_ID, this.sid)
                .set(CbsPostgres.CABINET.NAME, name)
                .set(CbsPostgres.CABINET.IS_DELETED, false)
                .returning()
                .fetchOne();
            if (insert == null) {
                throw new CabinetFailedCreateException();
            }
            result = new CbPostgres(this.datasource, selected.getId());
        } else {
            final var updated = this.datasource.update(CbsPostgres.CABINET)
                .set(CbsPostgres.CABINET.NAME, name)
                .where(CbsPostgres.CABINET.ID.eq(cid))
                .returning()
                .fetchOne();
            if (updated == null) {
                throw new CabinetFailedUpdateException();
            }
            result = new CbPostgres(this.datasource, updated.getId());
        }
        return result;
    }

    @Override
    public void remove(final long cid) throws Exception {
        final CabinetRecord cabinet = this.datasource.selectFrom(CbsPostgres.CABINET)
            .where(CbsPostgres.CABINET.ID.eq(cid)
                .and(CbsPostgres.CABINET.SCHOOL_ID.eq(this.sid))
                .and(CbsPostgres.CABINET.IS_DELETED.eq(false))
            )
            .fetchOne();
        if (cabinet == null) {
            throw new CabinetNotFoundException(String.format("Cabinet with id=%d not found", cid));
        }
        this.datasource.transactionResult(
            config ->
                DSL.using(config).update(CbsPostgres.CABINET)
                    .set(CbsPostgres.CABINET.IS_DELETED, true)
                    .where(CbsPostgres.CABINET.ID.eq(cid))
                    .execute()
        );
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
