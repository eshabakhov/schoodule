/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.schoolclass;

import com.eshabakhov.surok.Page;
import com.eshabakhov.surok.PageableList;
import com.eshabakhov.surok.page.ResponsePageableList;
import com.eshabakhov.surok.school.SchoolClass;
import com.eshabakhov.surok.school.SchoolClasses;
import com.eshabakhov.surok.sql.ClasspathSql;
import lombok.EqualsAndHashCode;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;

/**
 * Postgres implementation of {@link SchoolClasses}.
 *
 * @since 0.0.1
 * @checkstyle LambdaBodyLengthCheck (1000 lines)
 */
@EqualsAndHashCode
@SuppressWarnings({"PMD.AvoidCatchingGenericException", "PMD.PreserveStackTrace"})
public final class ScsPostgres implements SchoolClasses {

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    /** School ID. */
    private final Long sid;

    /**
     * New school classes collection.
     *
     * @param ctx Database context
     * @param sid School ID
     * @since 0.0.1
     */
    public ScsPostgres(final DSLContext ctx, final Long sid) {
        this.ctx = ctx;
        this.sid = sid;
    }

    @Override
    public SchoolClass create(final String litera, final Integer grade) throws Exception {
        try {
            return this.ctx.transactionResult(
                config -> {
                    final DSLContext ttx = DSL.using(config);
                    if (ttx.resultQuery(
                        new ClasspathSql("school-class/find-existing.sql").text(),
                        this.sid,
                        grade,
                        litera,
                        false
                    ).fetchOne() == null) {
                        final Record created = ttx.resultQuery(
                            new ClasspathSql("school-class/create.sql").text(),
                            this.sid,
                            grade,
                            litera,
                            false
                        ).fetchOne();
                        if (created == null) {
                            throw new SchoolClassFailedCreateException();
                        }
                        return new ScPostgres(this.ctx, created.get("id", Long.class));
                    } else {
                        throw new SchoolClassAlreadyExistsException(
                            String.format(
                                "SchoolClass with `%d%s` already exists",
                                grade, litera
                            )
                        );
                    }
                }
            );
            // @checkstyle IllegalCatchCheck (1 line)
        } catch (final Exception ex) {
            final Exception cause = (Exception) ex.getCause();
            if (cause == null) {
                throw ex;
            }
            throw cause;
        }
    }

    @Override
    public SchoolClass clazz(final long clazzid) throws Exception {
        final Record selected = this.ctx.resultQuery(
            new ClasspathSql("school-class/find-by-id-and-school-id.sql").text(),
            clazzid,
            this.sid,
            false
        ).fetchOne();
        if (selected == null) {
            throw new SchoolClassNotFoundException(
                String.format("SchoolClass `%s` not found", clazzid)
            );
        }
        return new ScPostgres(this.ctx, selected.get("id", Long.class));
    }

    @Override
    public PageableList<SchoolClass> classes(
        final Condition condition,
        final Page page
    ) throws Exception {
        return new ResponsePageableList<>(
            this.ctx.resultQuery(
                new ClasspathSql("school-class/list.sql").text(),
                condition.and(
                    DSL.condition(
                        new ClasspathSql("school-class/conditions/by-school-id.sql").text(),
                        this.sid
                    )
                ),
                DSL.val(page.limit()),
                DSL.val((page.offset() - 1) * page.limit())
            ).fetch(
                selected ->
                    new ScPostgres(this.ctx, selected.get("id", Long.class))
            ),
            this.ctx.resultQuery(
                new ClasspathSql("school-class/count.sql").text(),
                condition
            ).fetchSingle(0, Integer.class),
            page
        );
    }

    @Override
    public void remove(final long clazzid) throws Exception {
        final Record clazz = this.ctx.resultQuery(
            new ClasspathSql("school-class/find-by-id-and-school-id.sql").text(),
            clazzid,
            this.sid,
            false
        ).fetchOne();
        if (clazz == null) {
            throw new SchoolClassNotFoundException(
                String.format("SchoolClass with id=%d not found", clazzid)
            );
        }
        this.ctx.transactionResult(
            config ->
                DSL.using(config).resultQuery(
                    new ClasspathSql("school-class/soft-delete.sql").text(),
                    true,
                    clazzid
                ).execute()
        );
    }
}
