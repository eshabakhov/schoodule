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
                final var existing = ttx.fetchOne(
                    """
                    SELECT id
                    FROM public.cabinet
                    WHERE school_id = ?
                    AND name = ?
                    AND is_deleted = false
                    """,
                    this.sid, name
                );
                if (existing != null) {
                    throw new CabinetAlreadyExistsException(name);
                }
                final var created = ttx.fetchOne(
                    """
                    INSERT INTO public.cabinet (school_id, name, is_deleted)
                    VALUES (?, ?, false)
                    RETURNING id
                    """,
                    this.sid, name
                );
                if (created == null) {
                    throw new CabinetFailedCreateException();
                }
                return new CbPostgres(this.ctx, created.get("id", Long.class));
            }
        );
    }

    @Override
    public Cabinet find(final long cid) throws Exception {
        final var selected = this.ctx.fetchOne(
            """
            SELECT id
            FROM public.cabinet
            WHERE id = ?
            AND school_id = ?
            AND is_deleted = false
            """,
            cid, this.sid
        );
        if (selected == null) {
            throw new CabinetNotFoundException(String.format("Cabinet with id=%d not found", cid));
        }
        return new CbPostgres(this.ctx, selected.get("id", Long.class));
    }

    @Override
    public Cabinet find(final String name) throws Exception {
        final var selected = this.ctx.fetchOne(
            """
            SELECT id
            FROM public.cabinet
            WHERE school_id = ?
            AND name = ?
            AND is_deleted = false
            """,
            this.sid, name
        );
        if (selected == null) {
            throw new CabinetNotFoundException(
                String.format("Cabinet with name=`%s` not found", name)
            );
        }
        return new CbPostgres(this.ctx, selected.get("id", Long.class));
    }

    @Override
    public PageableList<Cabinet> list(final Condition condition, final Page page) throws Exception {
        return new ResponsePageableList<>(
            this.ctx.fetch(
                """
                SELECT id
                FROM public.cabinet
                WHERE school_id = ?
                AND is_deleted = false
                ORDER BY name ASC
                LIMIT ?
                OFFSET ?
                """,
                this.sid, page.limit(), (page.offset() - 1) * page.limit()
            ).map(r -> new CbPostgres(this.ctx, r.get("id", Long.class))),
            this.ctx
                .fetchOne(
                    """
                    SELECT COUNT(*)
                    FROM public.cabinet
                    WHERE school_id = ?
                    AND is_deleted = false
                    """,
                    this.sid
                )
                .get(0, Integer.class),
            page
        );
    }

    @Override
    public Cabinet put(final Long cid, final String name) throws Exception {
        final var selected = this.ctx.fetchOne(
            """
            SELECT id
            FROM public.cabinet
            WHERE id = ?
            AND school_id = ?
            AND is_deleted = false
            """,
            cid, this.sid
        );
        final Cabinet result;
        if (selected == null) {
            final var inserted = this.ctx.fetchOne(
                """
                INSERT INTO public.cabinet (school_id, name, is_deleted)
                VALUES (?, ?, false)
                RETURNING id
                """,
                this.sid, name
            );
            if (inserted == null) {
                throw new CabinetFailedCreateException();
            }
            result = new CbPostgres(this.ctx, inserted.get("id", Long.class));
        } else {
            final var updated = this.ctx.fetchOne(
                """
                UPDATE public.cabinet
                SET name = ?
                WHERE id = ?
                RETURNING id
                """,
                name, cid
            );
            if (updated == null) {
                throw new CabinetFailedUpdateException();
            }
            result = new CbPostgres(this.ctx, updated.get("id", Long.class));
        }
        return result;
    }

    @Override
    public void remove(final long cid) throws Exception {
        final var cabinet = this.ctx.fetchOne(
            """
            SELECT id
            FROM public.cabinet
            WHERE id = ?
            AND school_id = ?
            AND is_deleted = false
            """,
            cid, this.sid
        );
        if (cabinet == null) {
            throw new CabinetNotFoundException(
                String.format("Cabinet with id=%d not found", cid)
            );
        }
        this.ctx.execute(
            """
            UPDATE public.cabinet
            SET is_deleted = true
            WHERE id = ?
            """,
            cid
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
