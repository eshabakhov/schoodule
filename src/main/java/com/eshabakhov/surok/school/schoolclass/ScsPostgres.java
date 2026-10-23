/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.schoolclass;

import com.eshabakhov.surok.Filters;
import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.Page;
import com.eshabakhov.surok.PrItems;
import com.eshabakhov.surok.Sorts;
import com.eshabakhov.surok.page.ResponsePage;
import com.eshabakhov.surok.school.schoolclass.filter.FlsCdSchoolClass;
import com.eshabakhov.surok.school.schoolclass.input.MdScInput;
import com.eshabakhov.surok.school.schoolclass.sort.StsJqSchoolClass;
import com.eshabakhov.surok.sql.ClasspathSql;
import java.util.Map;
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
public final class ScsPostgres implements SchoolClasses {

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    /** School id. */
    private final Long sid;

    /**
     * New classes collection.
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
    public SchoolClass create(final SchoolClassInput input) throws Exception {
        final Map<String, String> fields = input.print(new MdScInput()).print();
        return this.ctx.transactionResult(
            config -> {
                final DSLContext ttx = DSL.using(config);
                if (ttx.resultQuery(
                    new ClasspathSql("school-class/find-existing.sql").text(),
                    this.sid,
                    Integer.valueOf(fields.get("grade")),
                    fields.get("litera"),
                    false
                ).fetchOne() == null) {
                    final Record created = ttx.resultQuery(
                        new ClasspathSql("school-class/create.sql").text(),
                        this.sid,
                        Integer.valueOf(fields.get("grade")),
                        fields.get("litera"),
                        false
                    ).fetchOne();
                    if (created == null) {
                        throw new SchoolClassFailedCreateException();
                    }
                    return new ScPostgres(this.ctx, created.get("id", Long.class));
                } else {
                    throw new SchoolClassAlreadyExistsException(
                        String.format(
                            "School class `%s%s` already exists",
                            fields.get("grade"), fields.get("litera")
                        )
                    );
                }
            }
        );
    }

    @Override
    public SchoolClass clazz(final long cid) throws Exception {
        final Record selected = this.ctx.resultQuery(
            new ClasspathSql("school-class/find-by-id-and-school-id.sql").text(),
            cid,
            this.sid,
            false
        ).fetchOne();
        if (selected == null) {
            throw new SchoolClassNotFoundException(
                String.format("School class with id=%d not found", cid)
            );
        }
        return new ScPostgres(this.ctx, selected.get("id", Long.class));
    }

    @Override
    public SchoolClasses selection(
        final Filters filters,
        final Page page,
        final Sorts sorts
    ) throws Exception {
        final Condition scoped = new FlsCdSchoolClass(filters).condition().and(
            DSL.condition(
                new ClasspathSql("school-class/conditions/by-school-id-and-is-deleted.sql").text(),
                this.sid,
                false
            )
        );
        return new ScsSelected(
            this,
            this.ctx.resultQuery(
                new ClasspathSql("school-class/list.sql").text(),
                scoped,
                DSL.list(new StsJqSchoolClass(sorts).fields()),
                DSL.val(page.limit()),
                DSL.val((page.offset() - 1) * page.limit())
            ).fetch(selected -> new ScPostgres(this.ctx, selected.get("id", Long.class))),
            new ResponsePage(
                page,
                this.ctx.resultQuery(
                    new ClasspathSql("school-class/count.sql").text(), scoped
                ).fetchSingle(0, Integer.class)
            )
        );
    }

    @Override
    public Iterable<SchoolClass> iterate() {
        return this.ctx.resultQuery(
            new ClasspathSql("school-class/list-active.sql").text(), this.sid, false
        ).fetch(selected -> new ScPostgres(this.ctx, selected.get("id", Long.class)));
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return media.with(new PrItems("school-class", this.iterate()));
    }

    @Override
    public void remove(final long cid) throws Exception {
        if (this.ctx.resultQuery(
            new ClasspathSql("school-class/find-by-id-and-school-id.sql").text(),
            cid,
            this.sid,
            false
        ).fetchOne() == null) {
            throw new SchoolClassNotFoundException(
                String.format("School class with id=%d not found", cid)
            );
        }
        this.ctx.resultQuery(
            new ClasspathSql("school-class/soft-delete.sql").text(), true, cid
        ).execute();
    }
}
