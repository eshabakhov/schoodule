/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.schedule.load;

import com.eshabakhov.surok.Page;
import com.eshabakhov.surok.PageableList;
import com.eshabakhov.surok.page.ResponsePageableList;
import com.eshabakhov.surok.school.SchoolClass;
import com.eshabakhov.surok.school.Subject;
import com.eshabakhov.surok.sql.ClasspathSql;
import lombok.EqualsAndHashCode;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;

/**
 * Postgres implementation of {@link ClassLoads}.
 *
 * @since 0.0.1
 * @checkstyle LambdaBodyLengthCheck (1000 lines)
 */
@EqualsAndHashCode
public final class ClassLoadsPostgres implements ClassLoads {

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    /** Schedule ID. */
    private final Long sid;

    /**
     * New class loads collection.
     *
     * @param ctx Database context
     * @param sid Schedule ID
     * @since 0.0.1
     */
    public ClassLoadsPostgres(final DSLContext ctx, final Long sid) {
        this.ctx = ctx;
        this.sid = sid;
    }

    @Override
    public ClassLoad create(
        final SchoolClass clazz,
        final Subject subject,
        final Integer hours
    ) throws Exception {
        return this.ctx.transactionResult(
            config -> {
                final DSLContext ttx = DSL.using(config);
                if (ttx.resultQuery(
                    new ClasspathSql("class-load/find-existing.sql").text(),
                    this.sid,
                    clazz.uid(),
                    subject.uid()
                ).fetchOne() == null) {
                    final Record created = ttx.resultQuery(
                        new ClasspathSql("class-load/create.sql").text(),
                        this.sid,
                        clazz.uid(),
                        subject.uid(),
                        hours
                    ).fetchOne();
                    if (created == null) {
                        throw new LoadFailedCreateException();
                    }
                    return new ClassLoadPostgres(this.ctx, created.get("id", Long.class));
                } else {
                    throw new LoadAlreadyExistsException();
                }
            }
        );
    }

    @Override
    public ClassLoad load(final long lid) throws Exception {
        final Record selected = this.ctx.resultQuery(
            new ClasspathSql("class-load/find-by-id-and-schedule-id.sql").text(),
            lid,
            this.sid
        ).fetchOne();
        if (selected == null) {
            throw new LoadNotFoundException(
                String.format("Class load with id=%d not found", lid)
            );
        }
        return new ClassLoadPostgres(this.ctx, selected.get("id", Long.class));
    }

    @Override
    public PageableList<ClassLoad> list(
        final Condition condition,
        final Page page
    ) throws Exception {
        return new ResponsePageableList<>(
            this.ctx.resultQuery(
                new ClasspathSql("class-load/find-by-schedule-id.sql").text(),
                this.sid
            ).fetch(
                selected ->
                    new ClassLoadPostgres(
                        this.ctx,
                        selected.get("id", Long.class)
                    )
            ),
            this.ctx.resultQuery(
                new ClasspathSql("class-load/count.sql").text(),
                this.sid
            ).fetchSingle(0, Integer.class),
            page
        );
    }

    @Override
    public void remove(final long lid) throws Exception {
        final Record load = this.ctx.resultQuery(
            new ClasspathSql("class-load/find-by-id-and-schedule-id.sql").text(),
            lid,
            this.sid
        ).fetchOne();
        if (load == null) {
            throw new LoadNotFoundException(
                String.format("Class load with id=%d not found", lid)
            );
        }
        this.ctx.transactionResult(
            config ->
                DSL.using(config).resultQuery(
                    new ClasspathSql("class-load/delete.sql").text(),
                    lid
                ).execute()
        );
    }
}
