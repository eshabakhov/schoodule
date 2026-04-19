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
public final class SbsPostgres implements Subjects {

    /** JOOQ Table for Cabinet. */
    private static final com.eshabakhov.schoodule.tables.Subject SUBJECT =
        com.eshabakhov.schoodule.tables.Subject.SUBJECT;

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext datasource;

    /** School ID. */
    private final Long sid;

    public SbsPostgres(final DSLContext datasource, final Long sid) {
        this.datasource = datasource;
        this.sid = sid;
    }

    @Override
    public Subject create(final Subject subject) throws Exception {
        return this.datasource.transactionResult(
            config -> {
                final DSLContext ctx = DSL.using(config);
                final var rec = this.datasource.selectFrom(SbsPostgres.SUBJECT)
                    .where(
                        SbsPostgres.SUBJECT.SCHOOL_ID.eq(this.sid)
                            .and(SbsPostgres.SUBJECT.NAME.eq(subject.name()))
                            .and(SbsPostgres.SUBJECT.IS_DELETED.eq(false))
                    )
                    .fetchOne();
                if (rec == null) {
                    final var created = ctx.insertInto(SbsPostgres.SUBJECT)
                        .set(SbsPostgres.SUBJECT.SCHOOL_ID, this.sid)
                        .set(SbsPostgres.SUBJECT.NAME, subject.name())
                        .set(SbsPostgres.SUBJECT.IS_DELETED, false)
                        .returning()
                        .fetchOne();
                    if (created == null) {
                        throw new SubjectFailedCreateException();
                    }
                    return new SbPostgres(this.datasource, created.getId());
                } else {
                    throw new SubjectAlreadyExistsException(subject);
                }
            }
        );
    }

    @Override
    public Subject find(final long subid) throws Exception {
        final SubjectRecord selected = this.datasource.selectFrom(SbsPostgres.SUBJECT)
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
        return new SbPostgres(this.datasource, selected.getId());
    }

    @Override
    public Subject find(final String name) throws Exception {
        final SubjectRecord selected = this.datasource.selectFrom(SbsPostgres.SUBJECT)
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
        return new SbPostgres(this.datasource, selected.getId());
    }

    @Override
    public PageableList<Subject> list(final Condition condition, final Page page) throws Exception {
        return new ResponsePageableList<>(
            this.datasource.selectFrom(SbsPostgres.SUBJECT)
                .where(condition.and(SbsPostgres.SUBJECT.SCHOOL_ID.eq(this.sid)))
                .orderBy(SbsPostgres.SUBJECT.NAME.asc())
                .limit(page.limit())
                .offset((page.offset() - 1) * page.limit())
                .fetch(selected -> new SbPostgres(this.datasource, selected.getId())),
            this.datasource.fetchCount(
                this.datasource.selectFrom(SbsPostgres.SUBJECT).where(condition)
            ),
            page
        );
    }

    @Override
    public Subject put(final Subject subject) throws Exception {
        final SubjectRecord selected = this.datasource.selectFrom(SbsPostgres.SUBJECT)
            .where(
                SbsPostgres.SUBJECT.ID.eq(subject.uid())
                    .and(SbsPostgres.SUBJECT.SCHOOL_ID.eq(this.sid))
                    .and(SbsPostgres.SUBJECT.IS_DELETED.eq(false))
            )
            .fetchOne();
        final Subject result;
        if (selected == null) {
            final SubjectRecord insert =  this.datasource.insertInto(SbsPostgres.SUBJECT)
                .set(SbsPostgres.SUBJECT.SCHOOL_ID, this.sid)
                .set(SbsPostgres.SUBJECT.NAME, subject.name())
                .set(SbsPostgres.SUBJECT.IS_DELETED, false)
                .returning()
                .fetchOne();
            if (insert == null) {
                throw new SubjectFailedCreateException();
            }
            result = new SbPostgres(this.datasource, selected.getId());
        } else {
            final SubjectRecord updated = this.datasource.update(SbsPostgres.SUBJECT)
                .set(SbsPostgres.SUBJECT.NAME, subject.name())
                .where(SbsPostgres.SUBJECT.ID.eq(subject.uid()))
                .returning()
                .fetchOne();
            if (updated == null) {
                throw new SubjectFailedUpdateException();
            }
            result = new SbPostgres(this.datasource, updated.getId());
        }
        return result;
    }

    @Override
    public void remove(final long subid) throws Exception {
        final SubjectRecord subject = this.datasource.selectFrom(SbsPostgres.SUBJECT)
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
        this.datasource.transactionResult(
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
        public SubjectAlreadyExistsException(final Subject subject) {
            super(String.format("Subject `%s` already exists", subject.json()));
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
