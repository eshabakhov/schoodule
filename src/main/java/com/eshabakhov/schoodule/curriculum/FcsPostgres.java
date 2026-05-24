/*
 * В© 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.curriculum;

import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.enums.EducationLevelType;
import com.eshabakhov.schoodule.enums.StudyWeekType;
import com.eshabakhov.schoodule.page.ResponsePageableList;
import com.eshabakhov.schoodule.tables.records.FederalCurriculumRecord;
import lombok.EqualsAndHashCode;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

/**
 * Postgres implementation of {@link FederalCurriculums}.
 *
 * @since 0.0.1
 */
@EqualsAndHashCode
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public final class FcsPostgres implements FederalCurriculums {

    /** JOOQ Table for FederalCurriculum. */
    private static final com.eshabakhov.schoodule.tables.FederalCurriculum CURRICULUM =
        com.eshabakhov.schoodule.tables.FederalCurriculum.FEDERAL_CURRICULUM;

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    public FcsPostgres(final DSLContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public FederalCurriculum create(final FederalCurriculum curriculum)
        throws Exception {
        return this.ctx.transactionResult(
            config -> {
                final DSLContext ttx = DSL.using(config);
                final var existing = ttx.selectFrom(FcsPostgres.CURRICULUM)
                    .where(
                        FcsPostgres.CURRICULUM.EDUCATION_LEVEL.eq(
                            EducationLevelType.valueOf(curriculum.educationLevel().name())
                        ).and(
                            FcsPostgres.CURRICULUM.STUDY_WEEK_TYPE.eq(
                                StudyWeekType.valueOf(curriculum.studyWeekType().name())
                            )
                        ).and(FcsPostgres.CURRICULUM.VERSION.eq(curriculum.version()))
                            .and(FcsPostgres.CURRICULUM.ACADEMIC_YEAR.eq(curriculum.academicYear()))
                            .and(FcsPostgres.CURRICULUM.IS_DELETED.eq(false))
                    )
                    .fetchOne();
                if (existing != null) {
                    throw new CurriculumAlreadyExistsException(curriculum);
                }
                final FederalCurriculumRecord created = ttx
                    .insertInto(FcsPostgres.CURRICULUM)
                    .set(FcsPostgres.CURRICULUM.TITLE, curriculum.title())
                    .set(
                        FcsPostgres.CURRICULUM.EDUCATION_LEVEL,
                        EducationLevelType.valueOf(curriculum.educationLevel().name())
                    )
                    .set(
                        FcsPostgres.CURRICULUM.STUDY_WEEK_TYPE,
                        StudyWeekType.valueOf(curriculum.studyWeekType().name())
                    )
                    .set(FcsPostgres.CURRICULUM.VERSION, curriculum.version())
                    .set(
                        FcsPostgres.CURRICULUM.ACADEMIC_YEAR,
                        curriculum.academicYear()
                    )
                    .set(
                        FcsPostgres.CURRICULUM.DESCRIPTION,
                        curriculum.description()
                    )
                    .set(FcsPostgres.CURRICULUM.IS_DELETED, false)
                    .returning()
                    .fetchOne();
                if (created == null) {
                    throw new CurriculumFailedCreateException();
                }
                return new FcPostgres(this.ctx, created.getId());
            }
        );
    }

    @Override
    public FederalCurriculum curriculum(final long id) throws Exception {
        final FederalCurriculumRecord selected = this.ctx
            .selectFrom(FcsPostgres.CURRICULUM)
            .where(
                FcsPostgres.CURRICULUM.ID.eq(id)
                    .and(FcsPostgres.CURRICULUM.IS_DELETED.eq(false))
            )
            .fetchOne();
        if (selected == null) {
            throw new CurriculumNotFoundException(
                String.format("FederalCurriculum with id=%d not found", id)
            );
        }
        return new FcPostgres(this.ctx, selected.getId());
    }

    @Override
    public PageableList<FederalCurriculum> curriculums(
        final Condition condition,
        final Page page
    ) throws Exception {
        final Condition scoped = condition.and(
            FcsPostgres.CURRICULUM.IS_DELETED.eq(false)
        );
        return new ResponsePageableList<>(
            this.ctx
                .selectFrom(FcsPostgres.CURRICULUM)
                .where(scoped)
                .orderBy(
                    FcsPostgres.CURRICULUM.ACADEMIC_YEAR.desc(),
                    FcsPostgres.CURRICULUM.TITLE.asc()
                )
                .limit(page.limit())
                .offset((page.offset() - 1) * page.limit())
                .fetch(
                    selected ->
                        new FcPostgres(this.ctx, selected.getId())
                ),
            this.ctx.fetchCount(
                this.ctx.selectFrom(FcsPostgres.CURRICULUM)
                    .where(scoped)
            ),
            page
        );
    }

    @Override
    public FederalCurriculum put(final FederalCurriculum curriculum)
        throws Exception {
        final FederalCurriculumRecord selected = this.ctx
            .selectFrom(FcsPostgres.CURRICULUM)
            .where(
                FcsPostgres.CURRICULUM.ID.eq(curriculum.uid())
                    .and(FcsPostgres.CURRICULUM.IS_DELETED.eq(false))
            )
            .fetchOne();
        final FederalCurriculum result;
        if (selected == null) {
            result = this.create(curriculum);
        } else {
            final FederalCurriculumRecord updated = this.ctx
                .update(FcsPostgres.CURRICULUM)
                .set(FcsPostgres.CURRICULUM.TITLE, curriculum.title())
                .set(
                    FcsPostgres.CURRICULUM.EDUCATION_LEVEL,
                    EducationLevelType.valueOf(curriculum.educationLevel().name())
                )
                .set(
                    FcsPostgres.CURRICULUM.STUDY_WEEK_TYPE,
                    StudyWeekType.valueOf(curriculum.studyWeekType().name())
                )
                .set(FcsPostgres.CURRICULUM.VERSION, curriculum.version())
                .set(
                    FcsPostgres.CURRICULUM.ACADEMIC_YEAR,
                    curriculum.academicYear()
                )
                .set(
                    FcsPostgres.CURRICULUM.DESCRIPTION,
                    curriculum.description()
                )
                .set(FcsPostgres.CURRICULUM.UPDATED_AT, DSL.currentOffsetDateTime())
                .where(FcsPostgres.CURRICULUM.ID.eq(curriculum.uid()))
                .returning()
                .fetchOne();
            if (updated == null) {
                throw new CurriculumFailedUpdateException();
            }
            result = new FcPostgres(this.ctx, updated.getId());
        }
        return result;
    }

    @Override
    public void remove(final long id) throws Exception {
        final FederalCurriculumRecord selected = this.ctx
            .selectFrom(FcsPostgres.CURRICULUM)
            .where(
                FcsPostgres.CURRICULUM.ID.eq(id)
                    .and(FcsPostgres.CURRICULUM.IS_DELETED.eq(false))
            )
            .fetchOne();
        if (selected == null) {
            throw new CurriculumNotFoundException(
                String.format("FederalCurriculum with id=%d not found", id)
            );
        }
        this.ctx.transactionResult(
            config ->
                DSL.using(config)
                    .update(FcsPostgres.CURRICULUM)
                    .set(FcsPostgres.CURRICULUM.IS_DELETED, true)
                    .set(
                        FcsPostgres.CURRICULUM.UPDATED_AT,
                        DSL.currentOffsetDateTime()
                    )
                    .where(FcsPostgres.CURRICULUM.ID.eq(id))
                    .execute()
        );
    }

    public static class CurriculumFailedCreateException extends Exception {
        public CurriculumFailedCreateException() {
            super("Failed to create FederalCurriculum");
        }
    }

    public static class CurriculumAlreadyExistsException extends Exception {
        public CurriculumAlreadyExistsException(final FederalCurriculum curriculum) {
            super(String.format("FederalCurriculum `%s` already exists", curriculum.json()));
        }
    }

    public static class CurriculumFailedUpdateException extends Exception {
        public CurriculumFailedUpdateException() {
            super("Failed to update FederalCurriculum");
        }
    }

    public static class CurriculumNotFoundException extends Exception {
        public CurriculumNotFoundException(final String message) {
            super(message);
        }
    }
}
