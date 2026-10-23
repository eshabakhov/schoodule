/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.schedule;

import com.eshabakhov.surok.Filters;
import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.Page;
import com.eshabakhov.surok.PrItems;
import com.eshabakhov.surok.Sorts;
import com.eshabakhov.surok.page.ResponsePage;
import com.eshabakhov.surok.school.schedule.filter.FlsCdSchedule;
import com.eshabakhov.surok.school.schedule.input.MdSdInput;
import com.eshabakhov.surok.school.schedule.sort.StsJqSchedule;
import com.eshabakhov.surok.sql.ClasspathSql;
import lombok.EqualsAndHashCode;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

/**
 * Postgres implementation of {@link Schedules}.
 *
 * <p>Checked domain failures are unwrapped from transaction failures.</p>
 *
 * @since 0.0.1
 * @checkstyle LambdaBodyLengthCheck (1000 lines)
 */
@EqualsAndHashCode
public final class SdsPostgres implements Schedules {

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    /** School id. */
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
    @SuppressWarnings("PMD.PreserveStackTrace")
    public Schedule create(final ScheduleInput input) throws Exception {
        final String name = input.print(new MdSdInput()).print().get("name");
        try {
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
        } catch (final DataAccessException exception) {
            switch (exception.getCause()) {
                case ScheduleAlreadyExistsException duplicate -> throw duplicate;
                case ScheduleFailedCreateException failed -> throw failed;
                case null, default -> throw exception;
            }
        }
    }

    @Override
    public Schedule schedule(final long cid) throws Exception {
        final Record selected = this.ctx.resultQuery(
            new ClasspathSql("schedule/find-by-id-and-school-id.sql").text(),
            cid,
            this.sid,
            false
        ).fetchOne();
        if (selected == null) {
            throw new ScheduleNotFoundException(
                String.format("Schedule with id=%d not found", cid)
            );
        }
        return new SdPostgres(this.ctx, selected.get("id", Long.class));
    }

    @Override
    public Schedules selection(
        final Filters filters,
        final Page page,
        final Sorts sorts
    ) throws Exception {
        final Condition scoped = new FlsCdSchedule(filters).condition().and(
            DSL.condition(
                new ClasspathSql("schedule/conditions/by-school-id-and-is-deleted.sql").text(),
                this.sid,
                false
            )
        );
        return new SdsSelected(
            this,
            this.ctx.resultQuery(
                new ClasspathSql("schedule/list.sql").text(),
                scoped,
                DSL.list(new StsJqSchedule(sorts).fields()),
                DSL.val(page.limit()),
                DSL.val((page.offset() - 1) * page.limit())
            ).fetch(selected -> new SdPostgres(this.ctx, selected.get("id", Long.class))),
            new ResponsePage(
                page,
                this.ctx.resultQuery(
                    new ClasspathSql("schedule/count.sql").text(), scoped
                ).fetchSingle(0, Integer.class)
            )
        );
    }

    @Override
    public Iterable<Schedule> iterate() {
        return this.ctx.resultQuery(
            new ClasspathSql("schedule/list-active.sql").text(), this.sid, false
        ).fetch(selected -> new SdPostgres(this.ctx, selected.get("id", Long.class)));
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return media.with(new PrItems("schedule", this.iterate()));
    }

    @Override
    public void remove(final long cid) throws Exception {
        if (this.ctx.resultQuery(
            new ClasspathSql("schedule/find-by-id-and-school-id.sql").text(),
            cid,
            this.sid,
            false
        ).fetchOne() == null) {
            throw new ScheduleNotFoundException(
                String.format("Schedule with id=%d not found", cid)
            );
        }
        this.ctx.resultQuery(
            new ClasspathSql("schedule/soft-delete.sql").text(), true, cid
        ).execute();
    }
}
