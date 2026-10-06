/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum;

import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.enums.EducationLevelType;
import com.eshabakhov.schoodule.enums.StudyWeekType;
import com.eshabakhov.schoodule.federal.curriculum.requirement.Requirements;
import com.eshabakhov.schoodule.federal.curriculum.requirement.RqsPostgres;
import com.eshabakhov.schoodule.tables.FederalCurriculum;
import org.jooq.DSLContext;

/**
 * Postgres implementation of {@link Curriculum}.
 * Holds only {@code fid} and {@code ctx} — all data is read from DB on demand.
 *
 * @since 0.0.1
 */
public final class CrPostgres implements Curriculum {

    /**
     * JOOQ table reference.
     */
    private static final FederalCurriculum CURRICULUM =
        FederalCurriculum.FEDERAL_CURRICULUM;

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
    public CrPostgres(final DSLContext ctx, final Long fid) {
        this.ctx = ctx;
        this.fid = fid;
    }

    @Override
    public Long uid() {
        return this.fid;
    }

    @Override
    public <M extends Media> M print(final M media) {
        this.ctx.selectFrom(CrPostgres.CURRICULUM)
            .where(CrPostgres.CURRICULUM.ID.eq(this.fid)).fetchOne(
                record -> media
                    .with("id", record.getId())
                    .with("title", record.getTitle())
                    .with("level", record.getEducationLevel().name())
                    .with("week", record.getStudyWeekType().name())
                    .with("version", record.getVersion())
                    .with("year", record.getAcademicYear())
                    .with("description", record.getDescription())
            );
        return media;
    }

    @Override
    public Curriculum retitled(final String title) {
        return new CrPostgres(
            this.ctx,
            this.ctx.update(CrPostgres.CURRICULUM)
                .set(CrPostgres.CURRICULUM.TITLE, title)
                .where(CrPostgres.CURRICULUM.ID.eq(this.fid))
                .returningResult(CrPostgres.CURRICULUM.ID)
                .fetchOne(CrPostgres.CURRICULUM.ID)
        );
    }

    @Override
    public Curriculum releveled(final Level level) {
        return new CrPostgres(
            this.ctx,
            this.ctx.update(CrPostgres.CURRICULUM).set(
                CrPostgres.CURRICULUM.EDUCATION_LEVEL,
                EducationLevelType.valueOf(level.name())
            ).where(CrPostgres.CURRICULUM.ID.eq(this.fid))
                .returningResult(CrPostgres.CURRICULUM.ID)
                .fetchOne(CrPostgres.CURRICULUM.ID)
        );
    }

    @Override
    public Curriculum reweeked(final Week week) {
        return new CrPostgres(
            this.ctx,
            this.ctx.update(CrPostgres.CURRICULUM).set(
                CrPostgres.CURRICULUM.STUDY_WEEK_TYPE,
                StudyWeekType.valueOf(week.name())
            ).where(CrPostgres.CURRICULUM.ID.eq(this.fid))
                .returningResult(CrPostgres.CURRICULUM.ID)
                .fetchOne(CrPostgres.CURRICULUM.ID)
        );
    }

    @Override
    public Curriculum reversioned(final String version) {
        return new CrPostgres(
            this.ctx,
            this.ctx.update(CrPostgres.CURRICULUM)
                .set(CrPostgres.CURRICULUM.VERSION, version)
                .where(CrPostgres.CURRICULUM.ID.eq(this.fid))
                .returningResult(CrPostgres.CURRICULUM.ID)
                .fetchOne(CrPostgres.CURRICULUM.ID)
        );
    }

    @Override
    public Curriculum reyeared(final String year) {
        return new CrPostgres(
            this.ctx,
            this.ctx.update(CrPostgres.CURRICULUM)
                .set(CrPostgres.CURRICULUM.ACADEMIC_YEAR, year)
                .where(CrPostgres.CURRICULUM.ID.eq(this.fid))
                .returningResult(CrPostgres.CURRICULUM.ID)
                .fetchOne(CrPostgres.CURRICULUM.ID)
        );
    }

    @Override
    public Curriculum redescriptioned(final String description) {
        return new CrPostgres(
            this.ctx,
            this.ctx.update(CrPostgres.CURRICULUM)
                .set(CrPostgres.CURRICULUM.DESCRIPTION, description)
                .where(CrPostgres.CURRICULUM.ID.eq(this.fid))
                .returningResult(CrPostgres.CURRICULUM.ID)
                .fetchOne(CrPostgres.CURRICULUM.ID)
        );
    }

    @Override
    public Requirements requirements() {
        return new RqsPostgres(this.ctx, this.fid);
    }
}
