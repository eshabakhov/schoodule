/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.schedule.load;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.Page;
import com.eshabakhov.surok.PrItems;
import com.eshabakhov.surok.page.ResponsePage;
import com.eshabakhov.surok.school.schedule.load.input.ClInputScoped;
import com.eshabakhov.surok.school.schedule.load.input.MdClInput;
import com.eshabakhov.surok.sql.ClasspathSql;
import java.util.Map;
import lombok.EqualsAndHashCode;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

/**
 * Postgres loads belonging to a schedule.
 *
 * <p>Checked domain failures are unwrapped from transaction failures.</p>
 *
 * @since 0.0.1
 */
@EqualsAndHashCode
public final class ClassLoadsPostgres implements ClassLoads {

    /** Database context. */
    private final DSLContext ctx;

    /** Schedule identifier. */
    private final Long sid;

    /**
     * New collection.
     *
     * @param ctx Database context
     * @param sid Schedule identifier
     */
    public ClassLoadsPostgres(final DSLContext ctx, final Long sid) {
        this.ctx = ctx;
        this.sid = sid;
    }

    @Override
    @SuppressWarnings("PMD.PreserveStackTrace")
    public ClassLoad create(final ClassLoadInput input) throws Exception {
        final Map<String, String> fields = new ClInputScoped(this.ctx, this.sid, input)
            .print(new MdClInput()).print();
        try {
            return this.ctx.transactionResult(
                config -> {
                    final DSLContext transaction = DSL.using(config);
                    if (transaction.resultQuery(
                        new ClasspathSql("class-load/find-existing.sql").text(), this.sid,
                        Long.valueOf(fields.get("schoolClassId")),
                        Long.valueOf(fields.get("subjectId"))
                    ).fetchOne() != null) {
                        throw new LoadAlreadyExistsException();
                    }
                    final Record created = transaction.resultQuery(
                        new ClasspathSql("class-load/create.sql").text(), this.sid,
                        Long.valueOf(fields.get("schoolClassId")),
                        Long.valueOf(fields.get("subjectId")),
                        Integer.valueOf(fields.get("hoursPerWeek"))
                    ).fetchOne();
                    if (created == null) {
                        throw new LoadFailedCreateException();
                    }
                    return new ClassLoadPostgres(this.ctx, created.get("id", Long.class));
                }
            );
        } catch (final DataAccessException exception) {
            switch (exception.getCause()) {
                case LoadAlreadyExistsException duplicate -> throw duplicate;
                case LoadFailedCreateException failed -> throw failed;
                case null, default -> throw exception;
            }
        }
    }

    @Override
    public ClassLoad load(final long id) throws Exception {
        if (this.ctx.resultQuery(
            new ClasspathSql("class-load/find-by-id-and-schedule-id.sql").text(), id, this.sid
        ).fetchOne() == null) {
            throw new LoadNotFoundException(String.format("Class load with id=%d not found", id));
        }
        return new ClassLoadPostgres(this.ctx, id);
    }

    @Override
    public ClassLoads selection(final Page page) throws Exception {
        return new ClsSelected(
            this,
            this.ctx.resultQuery(
                new ClasspathSql("class-load/list.sql").text(), this.sid,
                page.limit(), (page.offset() - 1) * page.limit()
            ).fetch(row -> new ClassLoadPostgres(this.ctx, row.get("id", Long.class))),
            new ResponsePage(
                page, this.ctx.resultQuery(
                    new ClasspathSql("class-load/count.sql").text(), this.sid
                ).fetchSingle(0, Integer.class)
            )
        );
    }

    @Override
    public Iterable<ClassLoad> iterate() {
        return this.ctx.resultQuery(
            new ClasspathSql("class-load/list-all.sql").text(), this.sid
        ).fetch(row -> new ClassLoadPostgres(this.ctx, row.get("id", Long.class)));
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return media.with(new PrItems("load", this.iterate()));
    }

    @Override
    public void remove(final long id) throws Exception {
        if (this.ctx.resultQuery(
            new ClasspathSql("class-load/delete.sql").text(), id, this.sid
        ).fetchOne() == null) {
            throw new LoadNotFoundException(String.format("Class load with id=%d not found", id));
        }
    }
}
