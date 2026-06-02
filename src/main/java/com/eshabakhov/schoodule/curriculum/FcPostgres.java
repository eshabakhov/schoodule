/*
 * В© 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.curriculum;

import com.eshabakhov.schoodule.enums.EducationLevelType;
import com.eshabakhov.schoodule.enums.StudyWeekType;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jooq.DSLContext;

/**
 * Postgres implementation of {@link FederalCurriculum}.
 *
 * @since 0.0.1
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class FcPostgres implements FederalCurriculum {

    /** JOOQ Table for FederalCurriculum. */
    private static final com.eshabakhov.schoodule.tables.FederalCurriculum CURRICULUM =
        com.eshabakhov.schoodule.tables.FederalCurriculum.FEDERAL_CURRICULUM;

    /** Federal curriculum id. */
    private final Long fid;

    /** Database connection. */
    private final DSLContext ctx;

    public FcPostgres(final DSLContext ctx, final Long fid) {
        this.ctx = ctx;
        this.fid = fid;
    }

    @Override
    public Long uid() {
        return this.fid;
    }

    @Override
    public String title() {
        return this.ctx.select(FcPostgres.CURRICULUM.TITLE)
            .from(FcPostgres.CURRICULUM)
            .where(FcPostgres.CURRICULUM.ID.eq(this.fid))
            .fetchOneInto(String.class);
    }

    @Override
    public Level level() {
        return Level.valueOf(
            this.ctx.select(FcPostgres.CURRICULUM.EDUCATION_LEVEL)
                .from(FcPostgres.CURRICULUM)
                .where(FcPostgres.CURRICULUM.ID.eq(this.fid))
                .fetchOneInto(String.class)
        );
    }

    @Override
    public Week week() {
        return Week.valueOf(
            this.ctx.select(FcPostgres.CURRICULUM.STUDY_WEEK_TYPE)
                .from(FcPostgres.CURRICULUM)
                .where(FcPostgres.CURRICULUM.ID.eq(this.fid))
                .fetchOneInto(String.class)
        );
    }

    @Override
    public String version() {
        return this.ctx.select(FcPostgres.CURRICULUM.VERSION)
            .from(FcPostgres.CURRICULUM)
            .where(FcPostgres.CURRICULUM.ID.eq(this.fid))
            .fetchOneInto(String.class);
    }

    @Override
    public String year() {
        return this.ctx.select(FcPostgres.CURRICULUM.ACADEMIC_YEAR)
            .from(FcPostgres.CURRICULUM)
            .where(FcPostgres.CURRICULUM.ID.eq(this.fid))
            .fetchOneInto(String.class);
    }

    @Override
    public String description() {
        return this.ctx.select(FcPostgres.CURRICULUM.DESCRIPTION)
            .from(FcPostgres.CURRICULUM)
            .where(FcPostgres.CURRICULUM.ID.eq(this.fid))
            .fetchOneInto(String.class);
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

    @Override
    public ObjectNode json() {
        return this.ctx.selectFrom(FcPostgres.CURRICULUM)
            .where(FcPostgres.CURRICULUM.ID.eq(this.fid))
            .fetchOne(
                selected ->
                    JsonNodeFactory.instance.objectNode()
                        .put("id", selected.getId())
                        .put("title", selected.getTitle())
                        .put("level", selected.getEducationLevel().name())
                        .put("week", selected.getStudyWeekType().name())
                        .put("version", selected.getVersion())
                        .put("year", selected.getAcademicYear())
                        .put("description", selected.getDescription())
            );
    }
}
