/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.schedule.load;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.school.schedule.load.input.ClInputScoped;
import com.eshabakhov.surok.school.schedule.load.input.LoadInputException;
import com.eshabakhov.surok.school.schedule.load.input.MdClInput;
import com.eshabakhov.surok.sql.ClasspathSql;
import java.util.Map;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

/**
 * Postgres teaching load, including names needed to display historical assignments.
 *
 * <p>Checked domain failures are unwrapped from transaction failures.</p>
 *
 * @since 0.0.1
 * @checkstyle LambdaBodyLengthCheck (100 lines)
 */
public final class ClassLoadPostgres implements ClassLoad {

    /** Database context. */
    private final DSLContext ctx;

    /** Load identifier. */
    private final Long lid;

    /**
     * New load.
     *
     * @param ctx Database context
     * @param lid Load identifier
     */
    public ClassLoadPostgres(final DSLContext ctx, final Long lid) {
        this.ctx = ctx;
        this.lid = lid;
    }

    @Override
    public Long uid() {
        return this.lid;
    }

    @Override
    @SuppressWarnings("PMD.PreserveStackTrace")
    public ClassLoad revise(final ClassLoadInput input) throws Exception {
        try {
            return this.ctx.transactionResult(
                config -> {
                    final DSLContext transaction = DSL.using(config);
                    final Record current = transaction.resultQuery(
                        new ClasspathSql("class-load/find-by-id.sql").text(), this.lid
                    ).fetchOne();
                    if (current == null) {
                        throw new LoadNotFoundException(
                            String.format("Class load with id=%d not found", this.lid)
                        );
                    }
                    final long schedule = current.get("schedule_id", Long.class);
                    final Map<String, String> fields = new ClInputScoped(
                        transaction, schedule, input
                    ).print(new MdClInput()).print();
                    final Record existing = transaction.resultQuery(
                        new ClasspathSql("class-load/find-existing.sql").text(), schedule,
                        Long.valueOf(fields.get("schoolClassId")),
                        Long.valueOf(fields.get("subjectId"))
                    ).fetchOne();
                    if (existing != null && !this.lid.equals(existing.get("id", Long.class))) {
                        throw new LoadAlreadyExistsException();
                    }
                    if (transaction.resultQuery(
                        new ClasspathSql("class-load/revise.sql").text(),
                        Long.valueOf(fields.get("schoolClassId")),
                        Long.valueOf(fields.get("subjectId")),
                        Integer.valueOf(fields.get("hoursPerWeek")), this.lid, schedule
                    ).fetchOne() == null) {
                        throw new LoadNotFoundException(
                            String.format("Class load with id=%d not found", this.lid)
                        );
                    }
                    return new ClassLoadPostgres(this.ctx, this.lid);
                }
            );
        } catch (final DataAccessException exception) {
            switch (exception.getCause()) {
                case LoadAlreadyExistsException duplicate -> throw duplicate;
                case LoadNotFoundException missing -> throw missing;
                case LoadInputException invalid -> throw invalid;
                case null, default -> throw exception;
            }
        }
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        final Record selected = this.ctx.resultQuery(
            new ClasspathSql("class-load/find-details.sql").text(), this.lid
        ).fetchOne();
        if (selected == null) {
            throw new LoadNotFoundException(
                String.format("Class load with id=%d not found", this.lid)
            );
        }
        return media.with("id", selected.get("id", Long.class))
            .with("schoolClassId", selected.get("school_class_id", Long.class)).with(
                "schoolClassName", String.format(
                    "%d%s", selected.get("class_grade", Integer.class),
                    selected.get("class_litera", String.class)
                )
            ).with("subjectId", selected.get("subject_id", Long.class))
            .with("subjectName", selected.get("subject_name", String.class))
            .with("hoursPerWeek", selected.get("hours_per_week", Integer.class));
    }
}
