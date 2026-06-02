/*
 * В© 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.curriculum;

import com.eshabakhov.schoodule.enums.CurriculumPartType;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jooq.DSLContext;

/**
 * Postgres implementation of {@link FederalCurriculumRequirement}.
 *
 * @since 0.0.1
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class FcrPostgres implements FederalCurriculumRequirement {

    /** JOOQ Table for FederalCurriculumRequirement. */
    private static final com.eshabakhov.schoodule.tables.FederalCurriculumRequirement REQUIREMENT =
        com.eshabakhov.schoodule.tables.FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT;

    /** Federal curriculum requirement id. */
    private final Long rid;

    /** Database connection. */
    private final DSLContext ctx;

    public FcrPostgres(final DSLContext ctx, final Long rid) {
        this.ctx = ctx;
        this.rid = rid;
    }

    @Override
    public Long uid() {
        return this.rid;
    }

    @Override
    public FederalCurriculum curriculum() {
        return new FcPostgres(
            this.ctx,
            this.ctx.select(FcrPostgres.REQUIREMENT.FEDERAL_CURRICULUM_ID)
                .from(FcrPostgres.REQUIREMENT)
                .where(FcrPostgres.REQUIREMENT.ID.eq(this.rid))
                .fetchOneInto(Long.class)
        );
    }

    @Override
    public Integer grade() {
        return this.ctx.select(FcrPostgres.REQUIREMENT.GRADE)
            .from(FcrPostgres.REQUIREMENT)
            .where(FcrPostgres.REQUIREMENT.ID.eq(this.rid))
            .fetchOneInto(Integer.class);
    }

    @Override
    public String subjectName() {
        return this.ctx.select(FcrPostgres.REQUIREMENT.SUBJECT_NAME)
            .from(FcrPostgres.REQUIREMENT)
            .where(FcrPostgres.REQUIREMENT.ID.eq(this.rid))
            .fetchOneInto(String.class);
    }

    @Override
    public Integer weeklyHours() {
        return this.ctx.select(FcrPostgres.REQUIREMENT.WEEKLY_HOURS)
            .from(FcrPostgres.REQUIREMENT)
            .where(FcrPostgres.REQUIREMENT.ID.eq(this.rid))
            .fetchOneInto(Integer.class);
    }

    @Override
    public PartType partType() {
        return PartType.valueOf(
            this.ctx.select(FcrPostgres.REQUIREMENT.PART_TYPE)
                .from(FcrPostgres.REQUIREMENT)
                .where(FcrPostgres.REQUIREMENT.ID.eq(this.rid))
                .fetchOneInto(String.class)
        );
    }

    @Override
    public FederalCurriculumRequirement regraded(final Integer grade) {
        return new FcrPostgres(
            this.ctx,
            this.ctx.update(FcrPostgres.REQUIREMENT)
                .set(FcrPostgres.REQUIREMENT.GRADE, grade)
                .where(FcrPostgres.REQUIREMENT.ID.eq(this.rid))
                .returningResult(FcrPostgres.REQUIREMENT.ID)
                .fetchOne(FcrPostgres.REQUIREMENT.ID)
        );
    }

    @Override
    public FederalCurriculumRequirement resubjected(final String subject) {
        return new FcrPostgres(
            this.ctx,
            this.ctx.update(FcrPostgres.REQUIREMENT)
                .set(FcrPostgres.REQUIREMENT.SUBJECT_NAME, subject)
                .where(FcrPostgres.REQUIREMENT.ID.eq(this.rid))
                .returningResult(FcrPostgres.REQUIREMENT.ID)
                .fetchOne(FcrPostgres.REQUIREMENT.ID)
        );
    }

    @Override
    public FederalCurriculumRequirement reweekled(final Integer hours) {
        return new FcrPostgres(
            this.ctx,
            this.ctx.update(FcrPostgres.REQUIREMENT)
                .set(FcrPostgres.REQUIREMENT.WEEKLY_HOURS, hours)
                .where(FcrPostgres.REQUIREMENT.ID.eq(this.rid))
                .returningResult(FcrPostgres.REQUIREMENT.ID)
                .fetchOne(FcrPostgres.REQUIREMENT.ID)
        );
    }

    @Override
    public FederalCurriculumRequirement reparted(final PartType part) {
        return new FcrPostgres(
            this.ctx,
            this.ctx.update(FcrPostgres.REQUIREMENT)
                .set(FcrPostgres.REQUIREMENT.PART_TYPE, CurriculumPartType.valueOf(part.name()))
                .where(FcrPostgres.REQUIREMENT.ID.eq(this.rid))
                .returningResult(FcrPostgres.REQUIREMENT.ID)
                .fetchOne(FcrPostgres.REQUIREMENT.ID)
        );
    }

    @Override
    public ObjectNode json() {
        return this.ctx.selectFrom(FcrPostgres.REQUIREMENT)
            .where(FcrPostgres.REQUIREMENT.ID.eq(this.rid))
            .fetchOne(
                selected ->
                    JsonNodeFactory.instance.objectNode()
                        .put("id", selected.getId())
                        .put("curriculumId", selected.getFederalCurriculumId())
                        .put("grade", selected.getGrade())
                        .put("subjectName", selected.getSubjectName())
                        .put("weeklyHours", selected.getWeeklyHours())
                        .put("partType", selected.getPartType().name())
            );
    }
}
