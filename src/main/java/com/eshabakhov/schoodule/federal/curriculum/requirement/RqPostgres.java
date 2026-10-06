/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement;

import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.enums.CurriculumPartType;
import com.eshabakhov.schoodule.tables.FederalCurriculumRequirement;
import org.jooq.DSLContext;

/**
 * Postgres implementation of {@link Requirement}.
 * Holds only {@code rid} and {@code ctx} — all data is read from DB on demand.
 *
 * @since 0.0.1
 */
public final class RqPostgres implements Requirement {

    /**
     * JOOQ table reference.
     */
    private static final FederalCurriculumRequirement REQUIREMENT =
        FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT;

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
    public <M extends Media> M print(final M media) {
        this.ctx.selectFrom(RqPostgres.REQUIREMENT)
            .where(RqPostgres.REQUIREMENT.ID.eq(this.rid)).fetchOne(
                record -> media
                    .with("id", record.getId())
                    .with("grade", record.getGrade())
                    .with("subjectName", record.getSubjectName())
                    .with("weeklyHours", record.getWeeklyHours())
                    .with("partType", record.getPartType().name())
            );
        return media;
    }

    @Override
    public Requirement regraded(final Integer grade) {
        return new RqPostgres(
            this.ctx,
            this.ctx.update(RqPostgres.REQUIREMENT)
                .set(RqPostgres.REQUIREMENT.GRADE, grade)
                .where(RqPostgres.REQUIREMENT.ID.eq(this.rid))
                .returningResult(RqPostgres.REQUIREMENT.ID)
                .fetchOne(RqPostgres.REQUIREMENT.ID)
        );
    }

    @Override
    public Requirement resubjected(final String subject) {
        return new RqPostgres(
            this.ctx,
            this.ctx.update(RqPostgres.REQUIREMENT)
                .set(RqPostgres.REQUIREMENT.SUBJECT_NAME, subject)
                .where(RqPostgres.REQUIREMENT.ID.eq(this.rid))
                .returningResult(RqPostgres.REQUIREMENT.ID)
                .fetchOne(RqPostgres.REQUIREMENT.ID)
        );
    }

    @Override
    public Requirement reweekled(final Integer hours) {
        return new RqPostgres(
            this.ctx,
            this.ctx.update(RqPostgres.REQUIREMENT)
                .set(RqPostgres.REQUIREMENT.WEEKLY_HOURS, hours)
                .where(RqPostgres.REQUIREMENT.ID.eq(this.rid))
                .returningResult(RqPostgres.REQUIREMENT.ID)
                .fetchOne(RqPostgres.REQUIREMENT.ID)
        );
    }

    @Override
    public Requirement reparted(final PartType part) {
        return new RqPostgres(
            this.ctx,
            this.ctx.update(RqPostgres.REQUIREMENT).set(
                RqPostgres.REQUIREMENT.PART_TYPE,
                CurriculumPartType.valueOf(part.name())
            ).where(RqPostgres.REQUIREMENT.ID.eq(this.rid))
                .returningResult(RqPostgres.REQUIREMENT.ID)
                .fetchOne(RqPostgres.REQUIREMENT.ID)
        );
    }
}
