/*
 * В© 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.curriculum;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jooq.DSLContext;

/**
 * Postgres implementation of {@link FederalCurriculum}.
 *
 * @since 0.0.1
 */
public final class FcPostgres implements FederalCurriculum {

    /** JOOQ Table for FederalCurriculum. */
    private static final com.eshabakhov.schoodule.tables.FederalCurriculum CURRICULUM =
        com.eshabakhov.schoodule.tables.FederalCurriculum.FEDERAL_CURRICULUM;

    /** Federal curriculum id. */
    private final long fid;

    /** Database connection. */
    private final DSLContext datasource;

    public FcPostgres(final DSLContext datasource, final long fid) {
        this.datasource = datasource;
        this.fid = fid;
    }

    @Override
    public Long uid() {
        return this.fid;
    }

    @Override
    public String title() {
        return this.datasource
            .select(FcPostgres.CURRICULUM.TITLE)
            .from(FcPostgres.CURRICULUM)
            .where(FcPostgres.CURRICULUM.ID.eq(this.fid))
            .fetchOneInto(String.class);
    }

    @Override
    public EducationLevel educationLevel() {
        return EducationLevel.valueOf(
            this.datasource
                .select(FcPostgres.CURRICULUM.EDUCATION_LEVEL)
                .from(FcPostgres.CURRICULUM)
                .where(FcPostgres.CURRICULUM.ID.eq(this.fid))
                .fetchOneInto(String.class)
        );
    }

    @Override
    public StudyWeek studyWeekType() {
        return StudyWeek.valueOf(
            this.datasource
                .select(FcPostgres.CURRICULUM.STUDY_WEEK_TYPE)
                .from(FcPostgres.CURRICULUM)
                .where(FcPostgres.CURRICULUM.ID.eq(this.fid))
                .fetchOneInto(String.class)
        );
    }

    @Override
    public String version() {
        return this.datasource
            .select(FcPostgres.CURRICULUM.VERSION)
            .from(FcPostgres.CURRICULUM)
            .where(FcPostgres.CURRICULUM.ID.eq(this.fid))
            .fetchOneInto(String.class);
    }

    @Override
    public String academicYear() {
        return this.datasource
            .select(FcPostgres.CURRICULUM.ACADEMIC_YEAR)
            .from(FcPostgres.CURRICULUM)
            .where(FcPostgres.CURRICULUM.ID.eq(this.fid))
            .fetchOneInto(String.class);
    }

    @Override
    public String description() {
        return this.datasource
            .select(FcPostgres.CURRICULUM.DESCRIPTION)
            .from(FcPostgres.CURRICULUM)
            .where(FcPostgres.CURRICULUM.ID.eq(this.fid))
            .fetchOneInto(String.class);
    }

    @Override
    public FederalCurriculumRequirements requirements() {
        return new FcrsPostgres(this.datasource, this.fid);
    }

    @Override
    public ObjectNode json() {
        return this.datasource
            .selectFrom(FcPostgres.CURRICULUM)
            .where(FcPostgres.CURRICULUM.ID.eq(this.fid))
            .fetchOne(
                selected ->
                    JsonNodeFactory.instance.objectNode()
                        .put("id", selected.getId())
                        .put("title", selected.getTitle())
                        .put("educationLevel", selected.getEducationLevel().name())
                        .put("studyWeekType", selected.getStudyWeekType().name())
                        .put("version", selected.getVersion())
                        .put("academicYear", selected.getAcademicYear())
                        .put("description", selected.getDescription())
            );
    }
}
