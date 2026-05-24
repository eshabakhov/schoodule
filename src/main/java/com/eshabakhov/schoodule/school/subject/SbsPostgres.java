/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.subject;

import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.page.ResponsePageableList;
import com.eshabakhov.schoodule.school.Subject;
import com.eshabakhov.schoodule.school.Subjects;
import com.eshabakhov.schoodule.tables.records.SubjectRecord;
import lombok.EqualsAndHashCode;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

/**
 * Postgres implementation of {@link Subjects}.
 *
 * @since 0.0.1
 */
@EqualsAndHashCode
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public final class SbsPostgres implements Subjects {

    /** JOOQ Table for Cabinet. */
    private static final com.eshabakhov.schoodule.tables.Subject SUBJECT =
        com.eshabakhov.schoodule.tables.Subject.SUBJECT;

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    /** School ID. */
    private final Long sid;

    public SbsPostgres(final DSLContext ctx, final Long sid) {
        this.ctx = ctx;
        this.sid = sid;
    }

    @Override
    public Subject create(final String name) throws Exception {
        return this.ctx.transactionResult(
            config -> {
                final DSLContext ttx = DSL.using(config);
                final var rec = ttx.selectFrom(SbsPostgres.SUBJECT)
                    .where(
                        SbsPostgres.SUBJECT.SCHOOL_ID.eq(this.sid)
                            .and(SbsPostgres.SUBJECT.NAME.eq(name))
                            .and(SbsPostgres.SUBJECT.IS_DELETED.eq(false))
                    )
                    .fetchOne();
                if (rec == null) {
                    final var created = ttx.insertInto(SbsPostgres.SUBJECT)
                        .set(SbsPostgres.SUBJECT.SCHOOL_ID, this.sid)
                        .set(SbsPostgres.SUBJECT.NAME, name)
                        .set(SbsPostgres.SUBJECT.IS_DELETED, false)
                        .returning()
                        .fetchOne();
                    if (created == null) {
                        throw new SubjectFailedCreateException();
                    }
                    return new SbPostgres(this.ctx, created.getId());
                } else {
                    throw new SubjectAlreadyExistsException(name);
                }
            }
        );
    }

    @Override
    public Subject subject(final long subid) throws Exception {
        final SubjectRecord selected = this.ctx.selectFrom(SbsPostgres.SUBJECT)
            .where(
                SbsPostgres.SUBJECT.ID.eq(subid)
                    .and(SbsPostgres.SUBJECT.SCHOOL_ID.eq(this.sid))
                    .and(SbsPostgres.SUBJECT.IS_DELETED.eq(false))
            )
            .fetchOne();
        if (selected == null) {
            throw new SubjectNotFoundException(
                String.format("Subject `%s` not found", subid)
            );
        }
        return new SbPostgres(this.ctx, selected.getId());
    }

    @Override
    public Subject subject(final String name) throws Exception {
        final SubjectRecord selected = this.ctx.selectFrom(SbsPostgres.SUBJECT)
            .where(
                SbsPostgres.SUBJECT.SCHOOL_ID.eq(this.sid)
                    .and(SbsPostgres.SUBJECT.NAME.eq(name))
                    .and(SbsPostgres.SUBJECT.IS_DELETED.eq(false))
            )
            .fetchOne();
        if (selected == null) {
            throw new SubjectNotFoundException(
                String.format("Subject `%s` not found", name)
            );
        }
        return new SbPostgres(this.ctx, selected.getId());
    }

    @Override
    public PageableList<Subject> subjects(final Condition condition, final Page page)
        throws Exception {
        return new ResponsePageableList<>(
            this.ctx.selectFrom(SbsPostgres.SUBJECT)
                .where(condition.and(SbsPostgres.SUBJECT.SCHOOL_ID.eq(this.sid)))
                .orderBy(SbsPostgres.SUBJECT.NAME.asc())
                .limit(page.limit())
                .offset((page.offset() - 1) * page.limit())
                .fetch(selected -> new SbPostgres(this.ctx, selected.getId())),
            this.ctx.fetchCount(
                this.ctx.selectFrom(SbsPostgres.SUBJECT).where(condition)
            ),
            page
        );
    }

    @Override
    public void remove(final long subid) throws Exception {
        final SubjectRecord subject = this.ctx.selectFrom(SbsPostgres.SUBJECT)
            .where(
                SbsPostgres.SUBJECT.ID.eq(subid)
                    .and(SbsPostgres.SUBJECT.SCHOOL_ID.eq(this.sid))
                    .and(SbsPostgres.SUBJECT.IS_DELETED.eq(false))
            )
            .fetchOne();
        if (subject == null) {
            throw new SubjectNotFoundException(
                String.format("Subject with id=%d not found", subid)
            );
        }
        this.ctx.transactionResult(
            config ->
                DSL.using(config).update(SbsPostgres.SUBJECT)
                    .set(SbsPostgres.SUBJECT.IS_DELETED, true)
                    .where(SbsPostgres.SUBJECT.ID.eq(subid))
                    .execute()
        );
    }

    public static class SubjectFailedCreateException extends Exception {
        public SubjectFailedCreateException() {
            super("Failed to create Subject");
        }
    }

    public static class SubjectAlreadyExistsException extends Exception {
        public SubjectAlreadyExistsException(final String name) {
            super(String.format("Subject `%s` already exists", name));
        }
    }

    public static class SubjectFailedUpdateException extends Exception {
        public SubjectFailedUpdateException() {
            super("Failed to update Subject");
        }
    }

    public static class SubjectNotFoundException extends Exception {
        public SubjectNotFoundException(final String message) {
            super(message);
        }
    }
}
