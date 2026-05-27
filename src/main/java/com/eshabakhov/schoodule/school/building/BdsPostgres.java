/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.building;

import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.page.ResponsePageableList;
import com.eshabakhov.schoodule.school.Building;
import com.eshabakhov.schoodule.school.Buildings;
import lombok.EqualsAndHashCode;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

/**
 * Postgres implementation of {@link Buildings}.
 *
 * @since 0.0.1
 */
@EqualsAndHashCode
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public final class BdsPostgres implements Buildings {

    /** JOOQ Table for Building. */
    private static final com.eshabakhov.schoodule.tables.Building BUILDING =
        com.eshabakhov.schoodule.tables.Building.BUILDING;

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    /** School id. */
    private final Long sid;

    public BdsPostgres(final DSLContext ctx, final Long sid) {
        this.ctx = ctx;
        this.sid = sid;
    }

    @Override
    public Building create(final String name) throws Exception {
        return this.ctx.transactionResult(
            config -> {
                final DSLContext ttx = DSL.using(config);
                final var select = ttx.selectFrom(BdsPostgres.BUILDING)
                    .where(
                        BdsPostgres.BUILDING.SCHOOL_ID.eq(this.sid)
                            .and(BdsPostgres.BUILDING.NAME.eq(name))
                            .and(BdsPostgres.BUILDING.IS_DELETED.eq(false))
                    )
                    .fetchOne();
                if (select == null) {
                    final var created = ttx.insertInto(BdsPostgres.BUILDING)
                        .set(BdsPostgres.BUILDING.SCHOOL_ID, this.sid)
                        .set(BdsPostgres.BUILDING.NAME, name)
                        .set(BdsPostgres.BUILDING.IS_DELETED, false)
                        .returning()
                        .fetchOne();
                    if (created == null) {
                        throw new BuildingFailedCreateException();
                    }
                    return new BdPostgres(this.ctx, created.getId());
                } else {
                    throw new BuildingAlreadyExistsException(name);
                }
            }
        );
    }

    @Override
    public Building building(final long cid) throws Exception {
        final var selected = this.ctx.selectFrom(BdsPostgres.BUILDING)
            .where(
                BdsPostgres.BUILDING.ID.eq(cid)
                    .and(BdsPostgres.BUILDING.SCHOOL_ID.eq(this.sid))
                    .and(BdsPostgres.BUILDING.IS_DELETED.eq(false))
            )
            .fetchOne();
        if (selected == null) {
            throw new BuildingNotFoundException(
                String.format("Building with id=%d not found", cid)
            );
        }
        return new BdPostgres(this.ctx, selected.getId());
    }

    @Override
    public Building building(final String name) throws Exception {
        final var selected = this.ctx.selectFrom(BdsPostgres.BUILDING)
            .where(
                BdsPostgres.BUILDING.SCHOOL_ID.eq(this.sid)
                    .and(BdsPostgres.BUILDING.NAME.eq(name))
                    .and(BdsPostgres.BUILDING.IS_DELETED.eq(false))
            )
            .fetchOne();
        if (selected == null) {
            throw new BuildingNotFoundException(
                String.format("Building with name=`%s` not found", name)
            );
        }
        return new BdPostgres(this.ctx, selected.getId());
    }

    @Override
    public PageableList<Building> buildings(final Condition condition, final Page page)
        throws Exception {
        final var cnd = condition.and(BdsPostgres.BUILDING.SCHOOL_ID.eq(this.sid));
        return new ResponsePageableList<>(
            this.ctx.selectFrom(BdsPostgres.BUILDING)
                .where(cnd)
                .orderBy(BdsPostgres.BUILDING.NAME.asc())
                .limit(page.limit())
                .offset((page.offset() - 1) * page.limit())
                .fetch(
                    selected -> new BdPostgres(this.ctx, selected.getId())
                ),
            this.ctx.fetchCount(
                this.ctx.selectFrom(BdsPostgres.BUILDING).where(cnd)
            ),
            page
        );
    }

    @Override
    public void remove(final long cid) throws Exception {
        final var building = this.ctx.selectFrom(BdsPostgres.BUILDING)
            .where(BdsPostgres.BUILDING.ID.eq(cid)
                .and(BdsPostgres.BUILDING.SCHOOL_ID.eq(this.sid))
                .and(BdsPostgres.BUILDING.IS_DELETED.eq(false))
            )
            .fetchOne();
        if (building == null) {
            throw new BuildingNotFoundException(
                String.format("Building with id=%d not found", cid)
            );
        }
        this.ctx.update(BdsPostgres.BUILDING)
            .set(BdsPostgres.BUILDING.IS_DELETED, true)
            .where(BdsPostgres.BUILDING.ID.eq(cid))
            .execute();
    }

    public static class BuildingFailedCreateException extends Exception {
        public BuildingFailedCreateException() {
            super("Failed to create Building");
        }
    }

    public static class BuildingAlreadyExistsException extends Exception {
        public BuildingAlreadyExistsException(final String name) {
            super(String.format("Building `%s` already exists", name));
        }
    }

    public static class BuildingNotFoundException extends Exception {
        public BuildingNotFoundException(final String message) {
            super(message);
        }
    }
}
