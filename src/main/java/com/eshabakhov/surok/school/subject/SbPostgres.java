/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.subject;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.school.subject.input.MdSbInput;
import com.eshabakhov.surok.sql.ClasspathSql;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

/**
 * Postgres school subject.
 *
 * <p>Checked domain failures are unwrapped from transaction failures.</p>
 *
 * @since 0.0.1
 * @checkstyle LambdaBodyLengthCheck (100 lines)
 */
public final class SbPostgres implements Subject {

    /** Database context. */
    private final DSLContext ctx;

    /** Subject identifier. */
    private final Long id;

    /**
     * New subject.
     *
     * @param ctx Database context
     * @param id Subject identifier
     */
    public SbPostgres(final DSLContext ctx, final Long id) {
        this.ctx = ctx;
        this.id = id;
    }

    @Override
    public Long uid() {
        return this.id;
    }

    @Override
    @SuppressWarnings("PMD.PreserveStackTrace")
    public Subject revise(final SubjectInput input) throws Exception {
        final String name = input.print(new MdSbInput()).print().get("name");
        try {
            return this.ctx.transactionResult(
                config -> {
                    final DSLContext transaction = DSL.using(config);
                    final Record current = transaction.resultQuery(
                        new ClasspathSql("subject/find-details.sql").text(), this.id, false
                    ).fetchOne();
                    if (current == null) {
                        throw new SubjectNotFoundException(
                            String.format("Subject with id=%d not found", this.id)
                        );
                    }
                    final Record existing = transaction.resultQuery(
                        new ClasspathSql("subject/find-existing.sql").text(),
                        current.get("school_id", Long.class), name, false
                    ).fetchOne();
                    if (existing != null && !this.id.equals(existing.get("id", Long.class))) {
                        throw new SubjectAlreadyExistsException(
                            String.format("Subject `%s` already exists", name)
                        );
                    }
                    final Long revised = transaction.resultQuery(
                        new ClasspathSql("subject/rename.sql").text(), name, this.id, false
                    ).fetchOne("id", Long.class);
                    if (revised == null) {
                        throw new SubjectNotFoundException(
                            String.format("Subject with id=%d not found", this.id)
                        );
                    }
                    return new SbPostgres(this.ctx, revised);
                }
            );
        } catch (final DataAccessException exception) {
            switch (exception.getCause()) {
                case SubjectAlreadyExistsException duplicate -> throw duplicate;
                case SubjectNotFoundException missing -> throw missing;
                case null, default -> throw exception;
            }
        }
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        final Record selected = this.ctx.resultQuery(
            new ClasspathSql("subject/find-details.sql").text(), this.id, false
        ).fetchOne();
        if (selected == null) {
            throw new SubjectNotFoundException(
                String.format("Subject with id=%d not found", this.id)
            );
        }
        return media.with("id", selected.get("id", Long.class))
            .with("name", selected.get("name", String.class));
    }
}
