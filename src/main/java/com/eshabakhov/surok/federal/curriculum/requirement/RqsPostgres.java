/*
 * В© 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.federal.curriculum.requirement;

import com.eshabakhov.surok.Filters;
import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.Page;
import com.eshabakhov.surok.PrItems;
import com.eshabakhov.surok.Sorts;
import com.eshabakhov.surok.federal.curriculum.requirement.filter.FlsCdRequirement;
import com.eshabakhov.surok.federal.curriculum.requirement.input.MdRqInput;
import com.eshabakhov.surok.federal.curriculum.requirement.sort.StsJqRequirement;
import com.eshabakhov.surok.page.ResponsePage;
import com.eshabakhov.surok.sql.ClasspathSql;
import java.util.Map;
import lombok.EqualsAndHashCode;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;

/**
 * Postgres implementation of {@link Requirements}.
 *
 * @since 0.0.1
 * @checkstyle LambdaBodyLengthCheck (1000 lines)
 */
@EqualsAndHashCode
public final class RqsPostgres implements Requirements {

    /**
     * JOOQ DSL context for executing database queries.
     */
    private final DSLContext ctx;

    /**
     * Federal curriculum ID.
     */
    private final Long fid;

    /**
     * Ctor.
     *
     * @param ctx JOOQ DSL context
     * @param fid Federal curriculum ID
     * @since 0.0.1
     */
    public RqsPostgres(final DSLContext ctx, final Long fid) {
        this.ctx = ctx;
        this.fid = fid;
    }

    @Override
    public Requirement create(final RequirementInput input) throws Exception {
        final Map<String, String> fields = input.print(
            new MdRqInput()
        ).print();
        final Integer grade = Integer.valueOf(fields.get("grade"));
        final String part = Requirement.PartType.valueOf(
            fields.get("partType")
        ).name();
        return this.ctx.transactionResult(
            config -> {
                final DSLContext ttx = DSL.using(config);
                if (ttx.resultQuery(
                    new ClasspathSql("curriculum/requirement/find-existing.sql").text(),
                    this.fid,
                    grade,
                    fields.get("subjectName"),
                    part,
                    false
                ).fetchOne() != null) {
                    throw new RequirementAlreadyExistsException();
                }
                final Record created = ttx.resultQuery(
                    new ClasspathSql("curriculum/requirement/create.sql").text(),
                    this.fid,
                    grade,
                    fields.get("subjectName"),
                    Integer.valueOf(fields.get("weeklyHours")),
                    part,
                    false
                ).fetchOne();
                if (created == null) {
                    throw new RequirementFailedCreateException();
                }
                return new RqPostgres(this.ctx, created.get("id", Long.class));
            }
        );
    }

    @Override
    public Requirement requirement(final long id) throws Exception {
        final Record selected = this.ctx.resultQuery(
            new ClasspathSql(
                "curriculum/requirement/find-by-federal-curriculum-id-and-id.sql"
            ).text(),
            this.fid,
            false,
            id
        ).fetchOne();
        if (selected == null) {
            throw new RequirementNotFoundException(
                String.format("Requirement with id=%d not found", id)
            );
        }
        return new RqPostgres(this.ctx, selected.get("id", Long.class));
    }

    @Override
    public Requirements selection(
        final Filters filters,
        final Page page,
        final Sorts sorts
    ) throws Exception {
        final Condition scoped = DSL.condition(
            new ClasspathSql("curriculum/requirement/conditions/by-selection.sql").text(),
            DSL.val(this.fid),
            DSL.val(false),
            new FlsCdRequirement(filters).condition()
        );
        return new RqsSelected(
            this,
            this.ctx.resultQuery(
                new ClasspathSql("curriculum/requirement/list.sql").text(),
                scoped,
                DSL.list(new StsJqRequirement(sorts).fields()),
                DSL.val(page.limit()),
                DSL.val((page.offset() - 1) * page.limit())
            ).fetch(
                selected ->
                    new RqPostgres(this.ctx, selected.get("id", Long.class))
            ),
            new ResponsePage(
                page,
                this.ctx.resultQuery(
                    new ClasspathSql("curriculum/requirement/count.sql").text(),
                    scoped
                ).fetchSingle(0, Integer.class)
            )
        );
    }

    @Override
    public Iterable<Requirement> iterate() {
        return this.ctx.resultQuery(
            new ClasspathSql("curriculum/requirement/list-active.sql").text(),
            this.fid,
            false
        ).fetch(selected -> new RqPostgres(this.ctx, selected.get("id", Long.class)));
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return media.with(new PrItems("requirement", this.iterate()));
    }

    @Override
    public void remove(final long id) throws Exception {
        if (this.ctx.resultQuery(
            new ClasspathSql(
                "curriculum/requirement/find-by-federal-curriculum-id-and-id.sql"
            ).text(),
            this.fid,
            false,
            id
        ).fetchOne() == null) {
            throw new RequirementNotFoundException(
                String.format(
                    "Requirement with id=%d not found",
                    id
                )
            );
        }
        this.ctx.transactionResult(
            config ->
                DSL.using(config).resultQuery(
                    new ClasspathSql("curriculum/requirement/soft-delete.sql").text(),
                    true,
                    id
                ).execute()
        );
    }
}
