/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.schedule;

import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.page.ResponsePageableList;
import com.eshabakhov.schoodule.school.Schedule;
import com.eshabakhov.schoodule.school.Schedules;
import com.eshabakhov.schoodule.tables.records.ScheduleRecord;
import lombok.EqualsAndHashCode;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

/**
 * Postgres implementation of {@link Schedules}.
 *
 * @since 0.0.1
 * @checkstyle LambdaBodyLengthCheck (1000 lines)
 */
@EqualsAndHashCode
public final class SdsPostgres implements Schedules {

    /** JOOQ Table for Schedule. */
    private static final com.eshabakhov.schoodule.tables.Schedule SCHEDULE =
        com.eshabakhov.schoodule.tables.Schedule.SCHEDULE;

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    /** School ID. */
    private final Long sid;

    /**
     * New schedules collection.
     *
     * @param ctx Database context
     * @param sid School ID
     * @since 0.0.1
     */
    public SdsPostgres(final DSLContext ctx, final Long sid) {
        this.ctx = ctx;
        this.sid = sid;
    }

    @Override
    public Schedule create(final String name) {
        return this.ctx.transactionResult(
            config -> {
                final DSLContext ttx = DSL.using(config);
                if (ttx.selectFrom(SdsPostgres.SCHEDULE).where(
                    SdsPostgres.SCHEDULE.SCHOOL_ID.eq(this.sid).and(
                        SdsPostgres.SCHEDULE.NAME.eq(name)
                    ).and(SdsPostgres.SCHEDULE.IS_DELETED.eq(false))
                    )
                    .fetchOne() == null) {
                    final ScheduleRecord created = ttx.insertInto(SdsPostgres.SCHEDULE)
                        .set(SdsPostgres.SCHEDULE.SCHOOL_ID, this.sid)
                        .set(SdsPostgres.SCHEDULE.NAME, name)
                        .set(SdsPostgres.SCHEDULE.IS_DELETED, false)
                        .returning()
                        .fetchOne();
                    if (created == null) {
                        throw new ScheduleFailedCreateException();
                    }
                    return new SdPostgres(this.ctx, created.getId());
                } else {
                    throw new ScheduleAlreadyExistsException(
                        String.format("Schedule `%s` already exists", name)
                    );
                }
            }
        );
    }

    @Override
    public Schedule schedule(final long scheduleid) throws Exception {
        final ScheduleRecord selected = this.ctx.selectFrom(SdsPostgres.SCHEDULE).where(
            SdsPostgres.SCHEDULE.ID.eq(scheduleid).and(
                SdsPostgres.SCHEDULE.SCHOOL_ID.eq(this.sid)
            ).and(SdsPostgres.SCHEDULE.IS_DELETED.eq(false))
            )
            .fetchOne();
        if (selected == null) {
            throw new ScheduleNotFoundException(
                String.format("Schedule with id=%d not found", scheduleid)
            );
        }
        return new SdPostgres(this.ctx, selected.getId());
    }

    @Override
    public Schedule schedule(final String name) throws Exception {
        final ScheduleRecord selected = this.ctx.selectFrom(SdsPostgres.SCHEDULE).where(
            SdsPostgres.SCHEDULE.SCHOOL_ID.eq(this.sid).and(
                SdsPostgres.SCHEDULE.NAME.eq(name)
            ).and(SdsPostgres.SCHEDULE.IS_DELETED.eq(false))
            )
            .fetchOne();
        if (selected == null) {
            throw new ScheduleNotFoundException(
                String.format("Schedule with name='%s' not found", name)
            );
        }
        return new SdPostgres(this.ctx, selected.getId());
    }

    @Override
    public PageableList<Schedule> schedules(
        final Condition condition,
        final Page page
    ) throws Exception {
        return new ResponsePageableList<>(
            this.ctx.selectFrom(SdsPostgres.SCHEDULE)
                .where(condition.and(SdsPostgres.SCHEDULE.SCHOOL_ID.eq(this.sid)))
                .orderBy(SdsPostgres.SCHEDULE.NAME.asc())
                .limit(page.limit())
                .offset((page.offset() - 1) * page.limit()).fetch(
                    selected -> new SdPostgres(
                        this.ctx,
                        selected.getId()
                    )
                ),
            this.ctx.fetchCount(
                this.ctx.selectFrom(SdsPostgres.SCHEDULE).where(condition)
            ),
            page
        );
    }

    @Override
    public void remove(final long scheduleid) throws Exception {
        final ScheduleRecord selected = this.ctx.selectFrom(SdsPostgres.SCHEDULE).where(
            SdsPostgres.SCHEDULE.ID.eq(scheduleid).and(
                SdsPostgres.SCHEDULE.SCHOOL_ID.eq(this.sid)
            ).and(SdsPostgres.SCHEDULE.IS_DELETED.eq(false))
            )
            .fetchOne();
        if (selected == null) {
            throw new ScheduleNotFoundException(
                String.format("Schedule with id=%d not found", scheduleid)
            );
        }
        this.ctx.transactionResult(
            config ->
                DSL.using(config).update(SdsPostgres.SCHEDULE)
                    .set(SdsPostgres.SCHEDULE.IS_DELETED, true)
                    .where(SdsPostgres.SCHEDULE.ID.eq(scheduleid))
                    .execute()
        );
    }
}
