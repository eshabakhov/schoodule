/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement;

import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.enums.CurriculumPartType;
import com.eshabakhov.schoodule.federal.FederalCurriculumRequirement;
import org.jooq.DSLContext;

/**
 * Postgres implementation of {@link FederalCurriculumRequirement}.
 * Holds only {@code rid} and {@code ctx} — all data is read from DB on demand.
 *
 * @since 0.0.1
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class FcrPostgres implements FederalCurriculumRequirement {

    /**
     * JOOQ table reference.
     */
    private static final com.eshabakhov.schoodule.tables.FederalCurriculumRequirement REQUIREMENT =
        com.eshabakhov.schoodule.tables.FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT;

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
    public FcrPostgres(final DSLContext ctx, final Long rid) {
        this.ctx = ctx;
        this.rid = rid;
    }

    @Override
    public Long uid() {
        return this.rid;
    }

    @Override
    public Media print(final Media media) {
        return this.ctx.selectFrom(FcrPostgres.REQUIREMENT)
            .where(FcrPostgres.REQUIREMENT.ID.eq(this.rid))
            .fetchOne(
                record -> media
                    .with("id", record.getId())
                    .with("grade", record.getGrade())
                    .with("subjectName", record.getSubjectName())
                    .with("weeklyHours", record.getWeeklyHours())
                    .with("partType", record.getPartType().name())
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
}
