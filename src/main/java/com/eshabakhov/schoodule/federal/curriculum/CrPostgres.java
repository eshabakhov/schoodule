/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum;

import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.enums.EducationLevelType;
import com.eshabakhov.schoodule.enums.StudyWeekType;
import com.eshabakhov.schoodule.federal.curriculum.input.MdCrInput;
import com.eshabakhov.schoodule.federal.curriculum.requirement.Requirements;
import com.eshabakhov.schoodule.federal.curriculum.requirement.RqsPostgres;
import com.eshabakhov.schoodule.tables.FederalCurriculum;
import java.util.Map;
import org.jooq.DSLContext;

/**
 * Postgres implementation of {@link Curriculum}.
 * Holds only {@code fid} and {@code ctx} - all data is read from DB on demand.
 *
 * @since 0.0.1
 */
public final class CrPostgres implements Curriculum {

    /**
     * JOOQ table reference.
     */
    private static final FederalCurriculum CURRICULUM = FederalCurriculum.FEDERAL_CURRICULUM;

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
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return this.ctx.selectFrom(CrPostgres.CURRICULUM)
            .where(CrPostgres.CURRICULUM.ID.eq(this.fid)).fetchSingle(
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
    public Curriculum revise(final CurriculumInput input) throws Exception {
        final Map<String, String> fields = input.print(
            new MdCrInput()
        ).print();
        return new CrPostgres(
            this.ctx,
            this.ctx.update(CrPostgres.CURRICULUM)
                .set(CrPostgres.CURRICULUM.TITLE, fields.get("title")).set(
                    CrPostgres.CURRICULUM.EDUCATION_LEVEL,
                    EducationLevelType.valueOf(fields.get("level"))
                ).set(
                    CrPostgres.CURRICULUM.STUDY_WEEK_TYPE,
                    StudyWeekType.valueOf(fields.get("week"))
                )
                .set(CrPostgres.CURRICULUM.VERSION, fields.get("version")).set(
                    CrPostgres.CURRICULUM.ACADEMIC_YEAR,
                    fields.get("year")
                ).set(
                    CrPostgres.CURRICULUM.DESCRIPTION,
                    fields.get("description")
                )
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
