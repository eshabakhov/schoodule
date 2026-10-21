/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.federal.curriculum.requirement;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.federal.curriculum.requirement.input.MdRqInput;
import com.eshabakhov.surok.sql.ClasspathSql;
import java.util.Map;
import org.jooq.DSLContext;

/**
 * Postgres implementation of {@link Requirement}.
 * Holds only {@code rid} and {@code ctx} - all data is read from DB on demand.
 *
 * @since 0.0.1
 */
public final class RqPostgres implements Requirement {

    /**
     * Federal curriculum requirement id.
     */
    private final Long rid;

    /**
     * Database connection.
     */
    private final DSLContext ctx;

    /**
     * Creates a Postgres-backed requirement.
     *
     * @param ctx JOOQ DSL context
     * @param rid Requirement ID
     */
    public RqPostgres(final DSLContext ctx, final Long rid) {
        this.ctx = ctx;
        this.rid = rid;
    }

    @Override
    public Long uid() {
        return this.rid;
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return this.ctx.resultQuery(
            new ClasspathSql("curriculum/requirement/find-details.sql").text(),
            this.rid
        ).fetchSingle(
            record -> media
                .with("id", record.get("id", Long.class))
                .with("grade", record.get("grade", Integer.class))
                .with("subjectName", record.get("subject_name", String.class))
                .with("weeklyHours", record.get("weekly_hours", Integer.class))
                .with("partType", record.get("part_type", String.class))
        );
    }

    @Override
    public Requirement revise(final RequirementInput input) throws Exception {
        final Map<String, String> fields = input.print(
            new MdRqInput()
        ).print();
        return new RqPostgres(
            this.ctx,
            this.ctx.resultQuery(
                new ClasspathSql("curriculum/requirement/revise.sql").text(),
                Integer.valueOf(fields.get("grade")),
                fields.get("subjectName"),
                Integer.valueOf(fields.get("weeklyHours")),
                Requirement.PartType.valueOf(fields.get("partType")).name(),
                this.rid
            ).fetchOne("id", Long.class)
        );
    }
}
