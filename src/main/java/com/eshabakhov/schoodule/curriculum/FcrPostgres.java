/*
 * В© 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.curriculum;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jooq.DSLContext;

/**
 * Postgres implementation of {@link FederalCurriculumRequirement}.
 *
 * @since 0.0.1
 */
public final class FcrPostgres implements FederalCurriculumRequirement {

    /** JOOQ Table for FederalCurriculumRequirement. */
    private static final com.eshabakhov.schoodule.tables.FederalCurriculumRequirement REQUIREMENT =
        com.eshabakhov.schoodule.tables.FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT;

    /** Federal curriculum requirement id. */
    private final long rid;

    /** Database connection. */
    private final DSLContext datasource;

    public FcrPostgres(final DSLContext datasource, final long rid) {
        this.datasource = datasource;
        this.rid = rid;
    }

    @Override
    public Long uid() {
        return this.rid;
    }

    @Override
    public FederalCurriculum curriculum() {
        return new FcPostgres(
            this.datasource,
            this.datasource
                .select(FcrPostgres.REQUIREMENT.FEDERAL_CURRICULUM_ID)
                .from(FcrPostgres.REQUIREMENT)
                .where(FcrPostgres.REQUIREMENT.ID.eq(this.rid))
                .fetchOneInto(Long.class)
        );
    }

    @Override
    public Integer grade() {
        return this.datasource
            .select(FcrPostgres.REQUIREMENT.GRADE)
            .from(FcrPostgres.REQUIREMENT)
            .where(FcrPostgres.REQUIREMENT.ID.eq(this.rid))
            .fetchOneInto(Integer.class);
    }

    @Override
    public String subjectName() {
        return this.datasource
            .select(FcrPostgres.REQUIREMENT.SUBJECT_NAME)
            .from(FcrPostgres.REQUIREMENT)
            .where(FcrPostgres.REQUIREMENT.ID.eq(this.rid))
            .fetchOneInto(String.class);
    }

    @Override
    public Integer weeklyHours() {
        return this.datasource
            .select(FcrPostgres.REQUIREMENT.WEEKLY_HOURS)
            .from(FcrPostgres.REQUIREMENT)
            .where(FcrPostgres.REQUIREMENT.ID.eq(this.rid))
            .fetchOneInto(Integer.class);
    }

    @Override
    public PartType partType() {
        return PartType.valueOf(
            this.datasource
                .select(FcrPostgres.REQUIREMENT.PART_TYPE)
                .from(FcrPostgres.REQUIREMENT)
                .where(FcrPostgres.REQUIREMENT.ID.eq(this.rid))
                .fetchOneInto(String.class)
        );
    }

    @Override
    public ObjectNode json() {
        return this.datasource
            .selectFrom(FcrPostgres.REQUIREMENT)
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
