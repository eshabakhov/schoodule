/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.cabinet;

import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.page.ResponsePageableList;
import com.eshabakhov.schoodule.school.Cabinet;
import com.eshabakhov.schoodule.school.Cabinets;
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
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class CbsPostgres implements Cabinets {

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
                final var existing = ttx.select(DSL.field("id"))
                    .from("public.cabinet")
                    .where(
                        DSL.condition(
                        "school_id = ? AND name = ? AND is_deleted = false",
                            this.sid,
                            name
                        )
                    )
                    .fetchOne();
                if (existing != null) {
                    throw new CabinetAlreadyExistsException(name);
                }
                final var created = ttx.insertInto(DSL.table("public.cabinet"))
                    .columns(
                        DSL.field("school_id"),
                        DSL.field("name"),
                        DSL.field("is_deleted")
                    )
                    .values(this.sid, name, false)
                    .returning(DSL.field("id"))
                    .fetchOne();
                if (created == null) {
                    throw new CabinetFailedCreateException();
                }
                return new CbPostgres(this.ctx, created.get("id", Long.class));
            }
        );
    }

    @Override
    public Cabinet find(final long cid) throws Exception {
        final var selected = this.ctx.select(DSL.field("id"))
            .from("public.cabinet")
            .where(
                DSL.condition("id = ? AND school_id = ? AND is_deleted = false", cid, this.sid)
            )
            .fetchOne();
        if (selected == null) {
            throw new CabinetNotFoundException(
                String.format("Cabinet with id=%d not found", cid)
            );
        }
        return new CbPostgres(this.ctx, selected.get("id", Long.class));
    }

    @Override
    public Cabinet find(final String name) throws Exception {
        final var selected = this.ctx.select(DSL.field("id"))
            .from("public.cabinet")
            .where(
                DSL.condition(
                    "school_id = ? AND name = ? AND is_deleted = false",
                    this.sid,
                    name
                )
            )
            .fetchOne();
        if (selected == null) {
            throw new CabinetNotFoundException(
                String.format("Cabinet with name=`%s` not found", name)
            );
        }
        return new CbPostgres(this.ctx, selected.get("id", Long.class));
    }

    @Override
    public PageableList<Cabinet> list(
        final Condition condition,
        final Page page
    ) throws Exception {
        final var base = DSL.condition("school_id = ? AND is_deleted = false", this.sid)
            .and(condition);
        return new ResponsePageableList<>(
            this.ctx.select(DSL.field("id"))
                .from("public.cabinet")
                .where(base)
                .orderBy(DSL.field("name").asc())
                .limit(page.limit())
                .offset((page.offset() - 1) * page.limit())
                .fetch(
                    rec -> new CbPostgres(
                        this.ctx,
                        rec.get("id", Long.class)
                    )
                ),
            this.ctx.fetchCount(
                this.ctx.select()
                    .from("public.cabinet")
                    .where(base)
            ),
            page
        );
    }

    @Override
    public Cabinet put(final Long cid, final String name) throws Exception {
        final var selected = this.ctx.select(DSL.field("id"))
            .from("public.cabinet")
            .where(
                DSL.condition(
                    "id = ? AND school_id = ? AND is_deleted = false",
                    cid,
                    this.sid
                )
            )
            .fetchOne();
        final Cabinet result;
        if (selected == null) {
            final var inserted = this.ctx.insertInto(DSL.table("public.cabinet"))
                .columns(
                    DSL.field("school_id"),
                    DSL.field("name"),
                    DSL.field("is_deleted")
                )
                .values(this.sid, name, false)
                .returning(DSL.field("id"))
                .fetchOne();
            if (inserted == null) {
                throw new CabinetFailedCreateException();
            }
            result = new CbPostgres(this.ctx, inserted.get("id", Long.class));
        } else {
            final var updated = this.ctx.update(DSL.table("public.cabinet"))
                .set(DSL.field("name"), name)
                .where(
                    DSL.condition("id = ?", cid)
                )
                .returning(DSL.field("id"))
                .fetchOne();
            if (updated == null) {
                throw new CabinetFailedUpdateException();
            }
            result = new CbPostgres(this.ctx, updated.get("id", Long.class));
        }
        return result;
    }

    @Override
    public void remove(final long cid) throws Exception {
        final var cabinet = this.ctx.select(DSL.field("id"))
            .from("public.cabinet")
            .where(
                DSL.condition(
                    "id = ? AND school_id = ? AND is_deleted = false",
                    cid,
                    this.sid
                )
            )
            .fetchOne();
        if (cabinet == null) {
            throw new CabinetNotFoundException(
                String.format("Cabinet with id=%d not found", cid)
            );
        }
        this.ctx.update(DSL.table("public.cabinet"))
            .set(DSL.field("is_deleted"), true)
            .where(DSL.condition("id = ?", cid))
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
