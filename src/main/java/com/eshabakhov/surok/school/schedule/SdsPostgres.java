/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.schedule;

import com.eshabakhov.surok.Page;
import com.eshabakhov.surok.PageableList;
import com.eshabakhov.surok.page.ResponsePageableList;
import com.eshabakhov.surok.school.Schedule;
import com.eshabakhov.surok.school.Schedules;
import com.eshabakhov.surok.sql.ClasspathSql;
import lombok.EqualsAndHashCode;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;

/**
 * Postgres implementation of {@link Schedules}.
 *
 * @since 0.0.1
 * @checkstyle LambdaBodyLengthCheck (1000 lines)
 */
@EqualsAndHashCode
public final class SdsPostgres implements Schedules {

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
                if (ttx.resultQuery(
                    new ClasspathSql("schedule/find-existing.sql").text(),
                    this.sid,
                    name,
                    false
                ).fetchOne() == null) {
                    final Record created = ttx.resultQuery(
                        new ClasspathSql("schedule/create.sql").text(),
                        this.sid,
                        name,
                        false
                    ).fetchOne();
                    if (created == null) {
                        throw new ScheduleFailedCreateException();
                    }
                    return new SdPostgres(this.ctx, created.get("id", Long.class));
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
        final Record selected = this.ctx.resultQuery(
            new ClasspathSql("schedule/find-by-id-and-school-id.sql").text(),
            scheduleid,
            this.sid,
            false
        ).fetchOne();
        if (selected == null) {
            throw new ScheduleNotFoundException(
                String.format("Schedule with id=%d not found", scheduleid)
            );
        }
        return new SdPostgres(this.ctx, selected.get("id", Long.class));
    }

    @Override
    public Schedule schedule(final String name) throws Exception {
        final Record selected = this.ctx.resultQuery(
            new ClasspathSql("schedule/find-existing.sql").text(),
            this.sid,
            name,
            false
        ).fetchOne();
        if (selected == null) {
            throw new ScheduleNotFoundException(
                String.format("Schedule with name='%s' not found", name)
            );
        }
        return new SdPostgres(this.ctx, selected.get("id", Long.class));
    }

    @Override
    public PageableList<Schedule> schedules(
        final Condition condition,
        final Page page
    ) throws Exception {
        return new ResponsePageableList<>(
            this.ctx.resultQuery(
                new ClasspathSql("schedule/list.sql").text(),
                condition.and(
                    DSL.condition(
                        new ClasspathSql("schedule/conditions/by-school-id.sql").text(),
                        this.sid
                    )
                ),
                DSL.val(page.limit()),
                DSL.val((page.offset() - 1) * page.limit())
            ).fetch(
                selected -> new SdPostgres(
                    this.ctx,
                    selected.get("id", Long.class)
                )
            ),
            this.ctx.resultQuery(
                new ClasspathSql("schedule/count.sql").text(),
                condition
            ).fetchSingle(0, Integer.class),
            page
        );
    }

    @Override
    public void remove(final long scheduleid) throws Exception {
        final Record selected = this.ctx.resultQuery(
            new ClasspathSql("schedule/find-by-id-and-school-id.sql").text(),
            scheduleid,
            this.sid,
            false
        ).fetchOne();
        if (selected == null) {
            throw new ScheduleNotFoundException(
                String.format("Schedule with id=%d not found", scheduleid)
            );
        }
        this.ctx.transactionResult(
            config ->
                DSL.using(config).resultQuery(
                    new ClasspathSql("schedule/soft-delete.sql").text(),
                    true,
                    scheduleid
                ).execute()
        );
    }
}
