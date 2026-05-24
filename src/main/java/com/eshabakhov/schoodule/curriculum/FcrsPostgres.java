/*
 * В© 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.curriculum;

import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.enums.CurriculumPartType;
import com.eshabakhov.schoodule.page.ResponsePageableList;
import com.eshabakhov.schoodule.tables.records.FederalCurriculumRequirementRecord;
import lombok.EqualsAndHashCode;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

/**
 * Postgres implementation of {@link FederalCurriculumRequirements}.
 *
 * @since 0.0.1
 */
@EqualsAndHashCode
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public final class FcrsPostgres implements FederalCurriculumRequirements {

    /** JOOQ Table for FederalCurriculumRequirement. */
    private static final com.eshabakhov.schoodule.tables.FederalCurriculumRequirement REQUIREMENT =
        com.eshabakhov.schoodule.tables.FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT;

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    /** Federal curriculum ID. */
    private final Long fid;

    public FcrsPostgres(final DSLContext ctx, final Long fid) {
        this.ctx = ctx;
        this.fid = fid;
    }

    @Override
    public FederalCurriculumRequirement create(
        final FederalCurriculumRequirement requirement
    ) throws Exception {
        return this.ctx.transactionResult(
            config -> {
                final DSLContext ttx = DSL.using(config);
                final var existing = ttx.selectFrom(FcrsPostgres.REQUIREMENT)
                    .where(FcrsPostgres.REQUIREMENT.FEDERAL_CURRICULUM_ID.eq(this.fid)
                        .and(FcrsPostgres.REQUIREMENT.GRADE.eq(requirement.grade()))
                        .and(FcrsPostgres.REQUIREMENT.SUBJECT_NAME.eq(requirement.subjectName()))
                        .and(
                            FcrsPostgres.REQUIREMENT.PART_TYPE.eq(
                                CurriculumPartType.valueOf(requirement.partType().name())
                            )
                        )
                        .and(FcrsPostgres.REQUIREMENT.IS_DELETED.eq(false))
                    )
                    .fetchOne();
                if (existing != null) {
                    throw new RequirementAlreadyExistsException(requirement);
                }
                final FederalCurriculumRequirementRecord created = ttx
                    .insertInto(FcrsPostgres.REQUIREMENT)
                    .set(FcrsPostgres.REQUIREMENT.FEDERAL_CURRICULUM_ID, this.fid)
                    .set(FcrsPostgres.REQUIREMENT.GRADE, requirement.grade())
                    .set(FcrsPostgres.REQUIREMENT.SUBJECT_NAME, requirement.subjectName())
                    .set(FcrsPostgres.REQUIREMENT.WEEKLY_HOURS, requirement.weeklyHours())
                    .set(
                        FcrsPostgres.REQUIREMENT.PART_TYPE,
                        CurriculumPartType.valueOf(requirement.partType().name())
                    )
                    .set(FcrsPostgres.REQUIREMENT.IS_DELETED, false)
                    .returning()
                    .fetchOne();
                if (created == null) {
                    throw new RequirementFailedCreateException();
                }
                return new FcrPostgres(this.ctx, created.getId());
            }
        );
    }

    @Override
    public FederalCurriculumRequirement requirement(final long id)
        throws Exception {
        final FederalCurriculumRequirementRecord selected = this.ctx
            .selectFrom(FcrsPostgres.REQUIREMENT)
            .where(
                FcrsPostgres.REQUIREMENT.FEDERAL_CURRICULUM_ID.eq(this.fid)
                    .and(FcrsPostgres.REQUIREMENT.IS_DELETED.eq(false))
                    .and(FcrsPostgres.REQUIREMENT.ID.eq(id))
            )
            .fetchOne();
        if (selected == null) {
            throw new RequirementNotFoundException(
                String.format("FederalCurriculumRequirement with id=%d not found", id)
            );
        }
        return new FcrPostgres(this.ctx, selected.getId());
    }

    @Override
    public PageableList<FederalCurriculumRequirement> requirements(
        final Condition condition,
        final Page page
    ) throws Exception {
        final Condition scoped = FcrsPostgres.REQUIREMENT.FEDERAL_CURRICULUM_ID.eq(this.fid)
            .and(FcrsPostgres.REQUIREMENT.IS_DELETED.eq(false))
            .and(condition);
        return new ResponsePageableList<>(
            this.ctx
                .selectFrom(FcrsPostgres.REQUIREMENT)
                .where(scoped)
                .orderBy(
                    FcrsPostgres.REQUIREMENT.GRADE.asc(),
                    FcrsPostgres.REQUIREMENT.SUBJECT_NAME.asc(),
                    FcrsPostgres.REQUIREMENT.PART_TYPE.asc()
                )
                .limit(page.limit())
                .offset((page.offset() - 1) * page.limit())
                .fetch(
                    selected ->
                        new FcrPostgres(this.ctx, selected.getId())
                ),
            this.ctx.fetchCount(
                this.ctx.selectFrom(FcrsPostgres.REQUIREMENT).where(scoped)
            ),
            page
        );
    }

    @Override
    public FederalCurriculumRequirement put(final FederalCurriculumRequirement requirement)
        throws Exception {
        final FederalCurriculumRequirementRecord selected = this.ctx
            .selectFrom(FcrsPostgres.REQUIREMENT)
            .where(
                FcrsPostgres.REQUIREMENT.FEDERAL_CURRICULUM_ID.eq(this.fid)
                    .and(FcrsPostgres.REQUIREMENT.IS_DELETED.eq(false))
                    .and(FcrsPostgres.REQUIREMENT.ID.eq(requirement.uid()))
            )
            .fetchOne();
        final FederalCurriculumRequirement result;
        if (selected == null) {
            result = this.create(requirement);
        } else {
            final FederalCurriculumRequirementRecord updated = this.ctx
                .update(FcrsPostgres.REQUIREMENT)
                .set(FcrsPostgres.REQUIREMENT.GRADE, requirement.grade())
                .set(FcrsPostgres.REQUIREMENT.SUBJECT_NAME, requirement.subjectName())
                .set(FcrsPostgres.REQUIREMENT.WEEKLY_HOURS, requirement.weeklyHours())
                .set(
                    FcrsPostgres.REQUIREMENT.PART_TYPE,
                    CurriculumPartType.valueOf(requirement.partType().name())
                )
                .set(FcrsPostgres.REQUIREMENT.UPDATED_AT, DSL.currentOffsetDateTime())
                .where(FcrsPostgres.REQUIREMENT.ID.eq(requirement.uid()))
                .returning()
                .fetchOne();
            if (updated == null) {
                throw new RequirementFailedUpdateException();
            }
            result = new FcrPostgres(this.ctx, updated.getId());
        }
        return result;
    }

    @Override
    public void remove(final long id) throws Exception {
        final FederalCurriculumRequirementRecord selected = this.ctx
            .selectFrom(FcrsPostgres.REQUIREMENT)
            .where(
                FcrsPostgres.REQUIREMENT.FEDERAL_CURRICULUM_ID.eq(this.fid)
                    .and(FcrsPostgres.REQUIREMENT.IS_DELETED.eq(false))
                    .and(FcrsPostgres.REQUIREMENT.ID.eq(id))
            )
            .fetchOne();
        if (selected == null) {
            throw new RequirementNotFoundException(
                String.format(
                    "FederalCurriculumRequirement with id=%d not found",
                    id
                )
            );
        }
        this.ctx.transactionResult(
            config ->
                DSL.using(config)
                    .update(FcrsPostgres.REQUIREMENT)
                    .set(FcrsPostgres.REQUIREMENT.IS_DELETED, true)
                    .set(FcrsPostgres.REQUIREMENT.UPDATED_AT, DSL.currentOffsetDateTime())
                    .where(FcrsPostgres.REQUIREMENT.ID.eq(id))
                    .execute()
        );
    }

    public static class RequirementFailedCreateException extends Exception {
        public RequirementFailedCreateException() {
            super("Failed to create FederalCurriculumRequirement");
        }
    }

    public static class RequirementAlreadyExistsException extends Exception {
        public RequirementAlreadyExistsException(final FederalCurriculumRequirement requirement) {
            super(
                String.format(
                    "FederalCurriculumRequirement `%s` already exists",
                    requirement.json()
                )
            );
        }
    }

    public static class RequirementFailedUpdateException extends Exception {
        public RequirementFailedUpdateException() {
            super("Failed to update FederalCurriculumRequirement");
        }
    }

    public static class RequirementNotFoundException extends Exception {
        public RequirementNotFoundException(final String message) {
            super(message);
        }
    }
}
