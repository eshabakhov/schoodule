/*
 * В© 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.federal.curriculum;

import com.eshabakhov.surok.Filters;
import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.Page;
import com.eshabakhov.surok.PrItems;
import com.eshabakhov.surok.Sorts;
import com.eshabakhov.surok.federal.curriculum.filter.FlsCdCurriculum;
import com.eshabakhov.surok.federal.curriculum.input.MdCrInput;
import com.eshabakhov.surok.federal.curriculum.sort.StsJqCurriculum;
import com.eshabakhov.surok.page.ResponsePage;
import com.eshabakhov.surok.sql.ClasspathSql;
import java.util.Map;
import lombok.EqualsAndHashCode;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;

/**
 * Postgres implementation of {@link Curriculums}.
 *
 * @since 0.0.1
 * @checkstyle LambdaBodyLengthCheck (1000 lines)
 */
@EqualsAndHashCode
public final class CrsPostgres implements Curriculums {

    /**
     * JOOQ DSL context for executing database queries.
     */
    private final DSLContext ctx;

    /**
     * Ctor.
     *
     * @param ctx JOOQ DSL context
     * @since 0.0.1
     */
    public CrsPostgres(final DSLContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public Curriculum create(final CurriculumInput input) throws Exception {
        final Map<String, String> fields = input.print(
            new MdCrInput()
        ).print();
        final String level = Curriculum.Level.valueOf(
            fields.get("level")
        ).name();
        final String week = Curriculum.Week.valueOf(
            fields.get("week")
        ).name();
        return this.ctx.transactionResult(
            config -> {
                final DSLContext ttx = DSL.using(config);
                if (ttx.resultQuery(
                    new ClasspathSql("curriculum/find-existing.sql").text(),
                    level,
                    week,
                    fields.get("version"),
                    fields.get("year"),
                    false
                ).fetchOne() != null) {
                    throw new CurriculumAlreadyExistsException(
                        String.format(
                            "Curriculum `%s` already exists",
                            fields.get("title")
                        )
                    );
                }
                final Record created = ttx.resultQuery(
                    new ClasspathSql("curriculum/create.sql").text(),
                    fields.get("title"),
                    level,
                    week,
                    fields.get("version"),
                    fields.get("year"),
                    fields.get("description"),
                    false
                ).fetchOne();
                if (created == null) {
                    throw new CurriculumFailedCreateException();
                }
                return new CrPostgres(this.ctx, created.get("id", Long.class));
            }
        );
    }

    @Override
    public Curriculum curriculum(final long id) throws Exception {
        final Record selected = this.ctx.resultQuery(
            new ClasspathSql("curriculum/find-by-id.sql").text(),
            id,
            false
        ).fetchOne();
        if (selected == null) {
            throw new CurriculumNotFoundException(
                String.format("Curriculum with id=%d not found", id)
            );
        }
        return new CrPostgres(this.ctx, selected.get("id", Long.class));
    }

    @Override
    public Curriculums selection(
        final Filters filters,
        final Page page,
        final Sorts sorts
    ) throws Exception {
        final Condition scoped = new FlsCdCurriculum(filters).condition().and(
            DSL.condition(
                new ClasspathSql("curriculum/conditions/by-is-deleted.sql").text(),
                false
            )
        );
        return new CrsSelected(
            this,
            this.ctx.resultQuery(
                new ClasspathSql("curriculum/list.sql").text(),
                scoped,
                DSL.list(new StsJqCurriculum(sorts).fields()),
                DSL.val(page.limit()),
                DSL.val((page.offset() - 1) * page.limit())
            ).fetch(
                selected -> new CrPostgres(
                    this.ctx,
                    selected.get("id", Long.class)
                )
            ),
            new ResponsePage(
                page,
                this.ctx.resultQuery(
                    new ClasspathSql("curriculum/count.sql").text(),
                    scoped
                ).fetchSingle(0, Integer.class)
            )
        );
    }

    @Override
    public Iterable<Curriculum> iterate() {
        return this.ctx.resultQuery(
            new ClasspathSql("curriculum/list-active.sql").text(),
            false
        ).fetch(selected -> new CrPostgres(this.ctx, selected.get("id", Long.class)));
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return media.with(new PrItems("curriculum", this.iterate()));
    }

    @Override
    public void remove(final long id) throws Exception {
        if (this.ctx.resultQuery(
            new ClasspathSql("curriculum/find-by-id.sql").text(),
            id,
            false
        ).fetchOne() == null) {
            throw new CurriculumNotFoundException(
                String.format("Curriculum with id=%d not found", id)
            );
        }
        this.ctx.transactionResult(
            config ->
                DSL.using(config).resultQuery(
                    new ClasspathSql("curriculum/soft-delete.sql").text(),
                    true,
                    id
                ).execute()
        );
    }
}
