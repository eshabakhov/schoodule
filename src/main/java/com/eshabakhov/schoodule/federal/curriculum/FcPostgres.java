/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum;

import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.enums.EducationLevelType;
import com.eshabakhov.schoodule.enums.StudyWeekType;
import com.eshabakhov.schoodule.federal.FederalCurriculum;
import com.eshabakhov.schoodule.federal.FederalCurriculumRequirements;
import com.eshabakhov.schoodule.federal.curriculum.requirement.FcrsPostgres;
import org.jooq.DSLContext;

/**
 * Postgres implementation of {@link FederalCurriculum}.
 * Holds only {@code fid} and {@code ctx} — all data is read from DB on demand.
 *
 * @since 0.0.1
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class FcPostgres implements FederalCurriculum {

    /**
     * JOOQ table reference.
     */
    private static final com.eshabakhov.schoodule.tables.FederalCurriculum CURRICULUM =
        com.eshabakhov.schoodule.tables.FederalCurriculum.FEDERAL_CURRICULUM;

    /**
     * Federal curriculum id.
     */
    private final Long fid;

    /**
     * Database connection.
     */
    private final DSLContext ctx;

    /**
     * Creates a Postgres-backed curriculum.
     *
     * @param ctx JOOQ DSL context
     * @param fid Curriculum ID
     */
    public FcPostgres(final DSLContext ctx, final Long fid) {
        this.ctx = ctx;
        this.fid = fid;
    }

    @Override
    public Long uid() {
        return this.fid;
    }

    @Override
    public Media print(final Media media) {
        return this.ctx.selectFrom(FcPostgres.CURRICULUM)
            .where(FcPostgres.CURRICULUM.ID.eq(this.fid))
            .fetchOne(
                record -> media
                    .with("id", record.getId())
                    .with("title", record.getTitle())
                    .with("level", record.getEducationLevel().name())
                    .with("week", record.getStudyWeekType().name())
                    .with("version", record.getVersion())
                    .with("year", record.getAcademicYear())
                    .with("description", record.getDescription())
            );
    }

    @Override
    public FederalCurriculum retitled(final String title) {
        return new FcPostgres(
            this.ctx,
            this.ctx.update(FcPostgres.CURRICULUM)
                .set(FcPostgres.CURRICULUM.TITLE, title)
                .where(FcPostgres.CURRICULUM.ID.eq(this.fid))
                .returningResult(FcPostgres.CURRICULUM.ID)
                .fetchOne(FcPostgres.CURRICULUM.ID)
        );
    }

    @Override
    public FederalCurriculum releveled(final Level level) {
        return new FcPostgres(
            this.ctx,
            this.ctx.update(FcPostgres.CURRICULUM)
                .set(
                    FcPostgres.CURRICULUM.EDUCATION_LEVEL,
                    EducationLevelType.valueOf(level.name())
                )
                .where(FcPostgres.CURRICULUM.ID.eq(this.fid))
                .returningResult(FcPostgres.CURRICULUM.ID)
                .fetchOne(FcPostgres.CURRICULUM.ID)
        );
    }

    @Override
    public FederalCurriculum reweeked(final Week week) {
        return new FcPostgres(
            this.ctx,
            this.ctx.update(FcPostgres.CURRICULUM)
                .set(FcPostgres.CURRICULUM.STUDY_WEEK_TYPE, StudyWeekType.valueOf(week.name()))
                .where(FcPostgres.CURRICULUM.ID.eq(this.fid))
                .returningResult(FcPostgres.CURRICULUM.ID)
                .fetchOne(FcPostgres.CURRICULUM.ID)
        );
    }

    @Override
    public FederalCurriculum reversioned(final String version) {
        return new FcPostgres(
            this.ctx,
            this.ctx.update(FcPostgres.CURRICULUM)
                .set(FcPostgres.CURRICULUM.VERSION, version)
                .where(FcPostgres.CURRICULUM.ID.eq(this.fid))
                .returningResult(FcPostgres.CURRICULUM.ID)
                .fetchOne(FcPostgres.CURRICULUM.ID)
        );
    }

    @Override
    public FederalCurriculum reyeared(final String year) {
        return new FcPostgres(
            this.ctx,
            this.ctx.update(FcPostgres.CURRICULUM)
                .set(FcPostgres.CURRICULUM.ACADEMIC_YEAR, year)
                .where(FcPostgres.CURRICULUM.ID.eq(this.fid))
                .returningResult(FcPostgres.CURRICULUM.ID)
                .fetchOne(FcPostgres.CURRICULUM.ID)
        );
    }

    @Override
    public FederalCurriculum redescriptioned(final String description) {
        return new FcPostgres(
            this.ctx,
            this.ctx.update(FcPostgres.CURRICULUM)
                .set(FcPostgres.CURRICULUM.DESCRIPTION, description)
                .where(FcPostgres.CURRICULUM.ID.eq(this.fid))
                .returningResult(FcPostgres.CURRICULUM.ID)
                .fetchOne(FcPostgres.CURRICULUM.ID)
        );
    }

    @Override
    public FederalCurriculumRequirements requirements() {
        return new FcrsPostgres(this.ctx, this.fid);
    }
}
