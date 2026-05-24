/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.schoolclass;

import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.page.ResponsePageableList;
import com.eshabakhov.schoodule.school.SchoolClass;
import com.eshabakhov.schoodule.school.SchoolClasses;
import com.eshabakhov.schoodule.tables.records.SchoolClassRecord;
import lombok.EqualsAndHashCode;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

/**
 * Postgres implementation of {@link SchoolClasses}.
 *
 * @since 0.0.1
 */
@EqualsAndHashCode
@SuppressWarnings({"PMD.AvoidCatchingGenericException", "PMD.PreserveStackTrace"})
public final class ScsPostgres implements SchoolClasses {

    /** JOOQ Table for Schedule. */
    private static final com.eshabakhov.schoodule.tables.SchoolClass SCHOOL_CLASS =
        com.eshabakhov.schoodule.tables.SchoolClass.SCHOOL_CLASS;

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext datasource;

    /** School ID. */
    private final Long sid;

    public ScsPostgres(final DSLContext datasource, final Long sid) {
        this.datasource = datasource;
        this.sid = sid;
    }

    @Override
    public SchoolClass create(final String litera, final Integer grade) throws Exception {
        try {
            return this.datasource.transactionResult(
                config -> {
                    final DSLContext ctx = DSL.using(config);
                    final var rec = this.datasource.selectFrom(ScsPostgres.SCHOOL_CLASS)
                        .where(
                            ScsPostgres.SCHOOL_CLASS.SCHOOL_ID.eq(this.sid)
                                .and(ScsPostgres.SCHOOL_CLASS.GRADE.eq(grade))
                                .and(ScsPostgres.SCHOOL_CLASS.LITERA.eq(litera))
                                .and(ScsPostgres.SCHOOL_CLASS.IS_DELETED.eq(false))
                        )
                        .fetchOne();
                    if (rec == null) {
                        final var created = ctx.insertInto(ScsPostgres.SCHOOL_CLASS)
                            .set(ScsPostgres.SCHOOL_CLASS.SCHOOL_ID, this.sid)
                            .set(ScsPostgres.SCHOOL_CLASS.GRADE, grade)
                            .set(ScsPostgres.SCHOOL_CLASS.LITERA, litera)
                            .set(ScsPostgres.SCHOOL_CLASS.IS_DELETED, false)
                            .returning()
                            .fetchOne();
                        if (created == null) {
                            throw new SchoolClassFailedCreateException();
                        }
                        return new ScPostgres(this.datasource, created.getId());
                    } else {
                        throw new SchoolClassAlreadyExistsException(litera, grade);
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
        final SchoolClassRecord selected = this.datasource.selectFrom(ScsPostgres.SCHOOL_CLASS)
            .where(
                ScsPostgres.SCHOOL_CLASS.ID.eq(clazzid)
                    .and(ScsPostgres.SCHOOL_CLASS.SCHOOL_ID.eq(this.sid))
                    .and(ScsPostgres.SCHOOL_CLASS.IS_DELETED.eq(false))
            )
            .fetchOne();
        if (selected == null) {
            throw new SchoolClassNotFoundException(
                String.format("SchoolClass `%s` not found", clazzid)
            );
        }
        return new ScPostgres(this.datasource, selected.getId());
    }

    @Override
    public PageableList<SchoolClass> classes(
        final Condition condition,
        final Page page
    ) throws Exception {
        return new ResponsePageableList<>(
            this.datasource.selectFrom(ScsPostgres.SCHOOL_CLASS)
                .where(condition.and(ScsPostgres.SCHOOL_CLASS.SCHOOL_ID.eq(this.sid)))
                .orderBy(
                    ScsPostgres.SCHOOL_CLASS.GRADE.asc(),
                    ScsPostgres.SCHOOL_CLASS.LITERA.asc()
                )
                .limit(page.limit())
                .offset((page.offset() - 1) * page.limit())
                .fetch(
                    selected ->
                        new ScPostgres(this.datasource, selected.getId())
                ),
            this.datasource.fetchCount(
                this.datasource.selectFrom(ScsPostgres.SCHOOL_CLASS).where(condition)
            ),
            page
        );
    }

    @Override
    public void remove(final long clazzid) throws Exception {
        final SchoolClassRecord clazz = this.datasource.selectFrom(ScsPostgres.SCHOOL_CLASS)
            .where(
                ScsPostgres.SCHOOL_CLASS.ID.eq(clazzid)
                    .and(ScsPostgres.SCHOOL_CLASS.SCHOOL_ID.eq(this.sid))
                    .and(ScsPostgres.SCHOOL_CLASS.IS_DELETED.eq(false))
            )
            .fetchOne();
        if (clazz == null) {
            throw new SchoolClassNotFoundException(
                String.format("SchoolClass with id=%d not found", clazzid)
            );
        }
        this.datasource.transactionResult(
            config ->
                DSL.using(config).update(ScsPostgres.SCHOOL_CLASS)
                    .set(ScsPostgres.SCHOOL_CLASS.IS_DELETED, true)
                    .where(ScsPostgres.SCHOOL_CLASS.ID.eq(clazzid))
                    .execute()
        );
    }

    public static class SchoolClassFailedCreateException extends Exception {
        public SchoolClassFailedCreateException() {
            super("Failed to create SchoolClass");
        }
    }

    public static class SchoolClassAlreadyExistsException extends Exception {
        public SchoolClassAlreadyExistsException(final String litera, final Integer grade) {
            super(
                String.format(
                    "SchoolClass with `%d%s` already exists",
                    grade, litera
                )
            );
        }
    }

    public static class SchoolClassFailedUpdateException extends Exception {
        public SchoolClassFailedUpdateException() {
            super("Failed to update SchoolClass");
        }
    }

    public static class SchoolClassNotFoundException extends Exception {
        public SchoolClassNotFoundException(final String message) {
            super(message);
        }
    }
}
