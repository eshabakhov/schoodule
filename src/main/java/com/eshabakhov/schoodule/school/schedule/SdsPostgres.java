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
 */
@EqualsAndHashCode
public final class SdsPostgres implements Schedules {

    /** JOOQ Table for Schedule. */
    private static final com.eshabakhov.schoodule.tables.Schedule SCHEDULE =
        com.eshabakhov.schoodule.tables.Schedule.SCHEDULE;

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext datasource;

    /** School ID. */
    private final Long sid;

    public SdsPostgres(final DSLContext datasource, final Long sid) {
        this.datasource = datasource;
        this.sid = sid;
    }

    @Override
    public Schedule add(final String name) {
        return this.datasource.transactionResult(
            config -> {
                final DSLContext ctx = DSL.using(config);
                final var select = ctx.selectFrom(SdsPostgres.SCHEDULE)
                    .where(
                        SdsPostgres.SCHEDULE.SCHOOL_ID.eq(this.sid)
                            .and(SdsPostgres.SCHEDULE.NAME.eq(name))
                            .and(SdsPostgres.SCHEDULE.IS_DELETED.eq(false))
                    )
                    .fetchOne();
                if (select == null) {
                    final var created = ctx.insertInto(SdsPostgres.SCHEDULE)
                        .set(SdsPostgres.SCHEDULE.SCHOOL_ID, this.sid)
                        .set(SdsPostgres.SCHEDULE.NAME, name)
                        .set(SdsPostgres.SCHEDULE.IS_DELETED, false)
                        .returning()
                        .fetchOne();
                    if (created == null) {
                        throw new ScheduleFailedCreateException();
                    }
                    return new SdPostgres(this.datasource, created.getId());
                } else {
                    throw new ScheduleAlreadyExistsException(name);
                }
            }
        );
    }

    @Override
    public Schedule find(final long scheduleid) throws Exception {
        final ScheduleRecord selected = this.datasource.selectFrom(SdsPostgres.SCHEDULE)
            .where(
                SdsPostgres.SCHEDULE.ID.eq(scheduleid)
                    .and(SdsPostgres.SCHEDULE.SCHOOL_ID.eq(this.sid))
                    .and(SdsPostgres.SCHEDULE.IS_DELETED.eq(false))
            )
            .fetchOne();
        if (selected == null) {
            throw new ScheduleNotFoundException(
                String.format("Schedule with id=%d not found", scheduleid)
            );
        }
        return new SdPostgres(this.datasource, selected.getId());
    }

    @Override
    public Schedule find(final String name) throws Exception {
        final ScheduleRecord selected = this.datasource.selectFrom(SdsPostgres.SCHEDULE)
            .where(
                SdsPostgres.SCHEDULE.SCHOOL_ID.eq(this.sid)
                    .and(SdsPostgres.SCHEDULE.NAME.eq(name))
                    .and(SdsPostgres.SCHEDULE.IS_DELETED.eq(false))
            )
            .fetchOne();
        if (selected == null) {
            throw new ScheduleNotFoundException(
                String.format("Schedule with name='%s' not found", name)
            );
        }
        return new SdPostgres(this.datasource, selected.getId());
    }

    @Override
    public PageableList<Schedule> list(
        final Condition condition,
        final Page page
    ) throws Exception {
        return new ResponsePageableList<>(
            this.datasource.selectFrom(SdsPostgres.SCHEDULE)
                .where(condition.and(SdsPostgres.SCHEDULE.SCHOOL_ID.eq(this.sid)))
                .orderBy(SdsPostgres.SCHEDULE.NAME.asc())
                .limit(page.limit())
                .offset((page.offset() - 1) * page.limit())
                .fetch(
                    selected -> new SdPostgres(
                        this.datasource,
                        selected.getId()
                    )
                ),
            this.datasource.fetchCount(
                this.datasource.selectFrom(SdsPostgres.SCHEDULE).where(condition)
            ),
            page
        );
    }

    @Override
    public Schedule put(final Long schedid, final String name) throws Exception {
        final ScheduleRecord select = this.datasource.selectFrom(SdsPostgres.SCHEDULE)
            .where(
                SdsPostgres.SCHEDULE.ID.eq(schedid)
                    .and(SdsPostgres.SCHEDULE.SCHOOL_ID.eq(this.sid))
                    .and(SdsPostgres.SCHEDULE.IS_DELETED.eq(false))
            )
            .fetchOne();
        final Schedule result;
        if (select == null) {
            final var inserted = this.datasource.insertInto(SdsPostgres.SCHEDULE)
                .set(SdsPostgres.SCHEDULE.SCHOOL_ID, this.sid)
                .set(SdsPostgres.SCHEDULE.NAME, name)
                .set(SdsPostgres.SCHEDULE.IS_DELETED, false)
                .returning()
                .fetchOne();
            if (inserted == null) {
                throw new ScheduleFailedCreateException();
            }
            result = new SdPostgres(this.datasource, inserted.getId());
        } else {
            final var updated = this.datasource.update(SdsPostgres.SCHEDULE)
                .set(SdsPostgres.SCHEDULE.NAME, name)
                .where(SdsPostgres.SCHEDULE.ID.eq(schedid))
                .returning()
                .fetchOne();
            if (updated == null) {
                throw new ScheduleFailedUpdateException();
            }
            result = new SdPostgres(this.datasource, updated.getId());
        }
        return result;
    }

    @Override
    public void remove(final long scheduleid) throws Exception {
        final var selected = this.datasource.selectFrom(SdsPostgres.SCHEDULE)
            .where(
                SdsPostgres.SCHEDULE.ID.eq(scheduleid)
                    .and(SdsPostgres.SCHEDULE.SCHOOL_ID.eq(this.sid))
                    .and(SdsPostgres.SCHEDULE.IS_DELETED.eq(false))
            )
            .fetchOne();
        if (selected == null) {
            throw new ScheduleNotFoundException(
                String.format("Schedule with id=%d not found", scheduleid)
            );
        }
        this.datasource.transactionResult(
            config ->
                DSL.using(config).update(SdsPostgres.SCHEDULE)
                    .set(SdsPostgres.SCHEDULE.IS_DELETED, true)
                    .where(SdsPostgres.SCHEDULE.ID.eq(scheduleid))
                    .execute()
        );
    }

    public static class ScheduleFailedCreateException extends Exception {
        public ScheduleFailedCreateException() {
            super("Failed to create Schedule");
        }
    }

    public static class ScheduleAlreadyExistsException extends Exception {
        public ScheduleAlreadyExistsException(final String name) {
            super(String.format("Schedule `%s` already exists", name));
        }
    }

    public static class ScheduleFailedUpdateException extends Exception {
        public ScheduleFailedUpdateException() {
            super("Failed to update Schedule");
        }
    }

    public static class ScheduleNotFoundException extends Exception {
        public ScheduleNotFoundException(final String message) {
            super(message);
        }
    }
}
