/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.building;

import com.eshabakhov.surok.Page;
import com.eshabakhov.surok.PageableList;
import com.eshabakhov.surok.page.ResponsePageableList;
import com.eshabakhov.surok.school.Building;
import com.eshabakhov.surok.school.Buildings;
import com.eshabakhov.surok.sql.ClasspathSql;
import lombok.EqualsAndHashCode;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;

/**
 * Postgres implementation of {@link Buildings}.
 *
 * @since 0.0.1
 * @checkstyle LambdaBodyLengthCheck (1000 lines)
 */
@EqualsAndHashCode
public final class BdsPostgres implements Buildings {

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    /** School id. */
    private final Long sid;

    /**
     * New buildings collection.
     *
     * @param ctx Database context
     * @param sid School ID
     * @since 0.0.1
     */
    public BdsPostgres(final DSLContext ctx, final Long sid) {
        this.ctx = ctx;
        this.sid = sid;
    }

    @Override
    public Building create(final String name) throws Exception {
        return this.ctx.transactionResult(
            config -> {
                final DSLContext ttx = DSL.using(config);
                if (ttx.resultQuery(
                    new ClasspathSql("building/find-existing.sql").text(),
                    this.sid,
                    name,
                    false
                ).fetchOne() == null) {
                    final Record created = ttx.resultQuery(
                        new ClasspathSql("building/create.sql").text(),
                        this.sid,
                        name,
                        false
                    ).fetchOne();
                    if (created == null) {
                        throw new BuildingFailedCreateException();
                    }
                    return new BdPostgres(this.ctx, created.get("id", Long.class));
                } else {
                    throw new BuildingAlreadyExistsException(
                        String.format("Building `%s` already exists", name)
                    );
                }
            }
        );
    }

    @Override
    public Building building(final long cid) throws Exception {
        final Record selected = this.ctx.resultQuery(
            new ClasspathSql("building/find-by-id-and-school-id.sql").text(),
            cid,
            this.sid,
            false
        ).fetchOne();
        if (selected == null) {
            throw new BuildingNotFoundException(
                String.format("Building with id=%d not found", cid)
            );
        }
        return new BdPostgres(this.ctx, selected.get("id", Long.class));
    }

    @Override
    public Building building(final String name) throws Exception {
        final Record selected = this.ctx.resultQuery(
            new ClasspathSql("building/find-existing.sql").text(),
            this.sid,
            name,
            false
        ).fetchOne();
        if (selected == null) {
            throw new BuildingNotFoundException(
                String.format("Building with name=`%s` not found", name)
            );
        }
        return new BdPostgres(this.ctx, selected.get("id", Long.class));
    }

    @Override
    public PageableList<Building> buildings(final Condition condition, final Page page)
        throws Exception {
        final Condition cnd = condition.and(
            DSL.condition(
                new ClasspathSql("building/conditions/by-school-id.sql").text(),
                this.sid
            )
        );
        return new ResponsePageableList<>(
            this.ctx.resultQuery(
                new ClasspathSql("building/list.sql").text(),
                cnd,
                DSL.val(page.limit()),
                DSL.val((page.offset() - 1) * page.limit())
            ).fetch(
                selected -> new BdPostgres(this.ctx, selected.get("id", Long.class))
            ),
            this.ctx.resultQuery(
                new ClasspathSql("building/count.sql").text(),
                cnd
            ).fetchSingle(0, Integer.class),
            page
        );
    }

    @Override
    public void remove(final long cid) throws Exception {
        final Record building = this.ctx.resultQuery(
            new ClasspathSql("building/find-by-id-and-school-id.sql").text(),
            cid,
            this.sid,
            false
        ).fetchOne();
        if (building == null) {
            throw new BuildingNotFoundException(
                String.format("Building with id=%d not found", cid)
            );
        }
        this.ctx.resultQuery(
            new ClasspathSql("building/soft-delete.sql").text(),
            true,
            cid
        ).execute();
    }
}
