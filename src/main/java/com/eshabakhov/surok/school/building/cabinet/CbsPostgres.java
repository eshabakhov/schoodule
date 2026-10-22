/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.building.cabinet;

import com.eshabakhov.surok.Page;
import com.eshabakhov.surok.PageableList;
import com.eshabakhov.surok.page.ResponsePageableList;
import com.eshabakhov.surok.school.building.Cabinet;
import com.eshabakhov.surok.school.building.Cabinets;
import com.eshabakhov.surok.sql.ClasspathSql;
import lombok.EqualsAndHashCode;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;

/**
 * Postgres implementation of {@link Cabinets}.
 *
 * @since 0.0.1
 * @checkstyle LambdaBodyLengthCheck (1000 lines)
 */
@EqualsAndHashCode
public final class CbsPostgres implements Cabinets {

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    /** Building id. */
    private final Long bid;

    /**
     * New cabinets collection.
     *
     * @param ctx Database context
     * @param bid Building ID
     * @since 0.0.1
     */
    public CbsPostgres(final DSLContext ctx, final Long bid) {
        this.ctx = ctx;
        this.bid = bid;
    }

    @Override
    public Cabinet create(final String name) throws Exception {
        return this.ctx.transactionResult(
            config -> {
                final DSLContext ttx = DSL.using(config);
                if (ttx.resultQuery(
                    new ClasspathSql("cabinet/find-existing.sql").text(),
                    this.bid,
                    name,
                    false
                ).fetchOne() == null) {
                    final Record created = ttx.resultQuery(
                        new ClasspathSql("cabinet/create.sql").text(),
                        this.bid,
                        name,
                        false
                    ).fetchOne();
                    if (created == null) {
                        throw new CabinetFailedCreateException();
                    }
                    return new CbPostgres(this.ctx, created.get("id", Long.class));
                } else {
                    throw new CabinetAlreadyExistsException(
                        String.format("Cabinet `%s` already exists", name)
                    );
                }
            }
        );
    }

    @Override
    public Cabinet cabinet(final long cid) throws Exception {
        final Record selected = this.ctx.resultQuery(
            new ClasspathSql("cabinet/find-by-id-and-building-id.sql").text(),
            cid,
            this.bid,
            false
        ).fetchOne();
        if (selected == null) {
            throw new CabinetNotFoundException(
                String.format("Cabinet with id=%d not found", cid)
            );
        }
        return new CbPostgres(this.ctx, selected.get("id", Long.class));
    }

    @Override
    public Cabinet cabinet(final String name) throws Exception {
        final Record selected = this.ctx.resultQuery(
            new ClasspathSql("cabinet/find-existing.sql").text(),
            this.bid,
            name,
            false
        ).fetchOne();
        if (selected == null) {
            throw new CabinetNotFoundException(
                String.format("Cabinet with name=`%s` not found", name)
            );
        }
        return new CbPostgres(this.ctx, selected.get("id", Long.class));
    }

    @Override
    public PageableList<Cabinet> cabinets(final Condition condition, final Page page)
        throws Exception {
        final Condition cnd = condition.and(
            DSL.condition(
                new ClasspathSql("cabinet/conditions/by-building-id.sql").text(),
                this.bid
            )
        );
        return new ResponsePageableList<>(
            this.ctx.resultQuery(
                new ClasspathSql("cabinet/list.sql").text(),
                cnd,
                DSL.val(page.limit()),
                DSL.val((page.offset() - 1) * page.limit())
            ).fetch(
                selected -> new CbPostgres(this.ctx, selected.get("id", Long.class))
            ),
            this.ctx.resultQuery(
                new ClasspathSql("cabinet/count.sql").text(),
                cnd
            ).fetchSingle(0, Integer.class),
            page
        );
    }

    @Override
    public void remove(final long cid) throws Exception {
        final Record cabinet = this.ctx.resultQuery(
            new ClasspathSql("cabinet/find-by-id-and-building-id.sql").text(),
            cid,
            this.bid,
            false
        ).fetchOne();
        if (cabinet == null) {
            throw new CabinetNotFoundException(String.format("Cabinet with id=%d not found", cid));
        }
        this.ctx.resultQuery(
            new ClasspathSql("cabinet/soft-delete.sql").text(),
            true,
            cid
        ).execute();
    }
}
