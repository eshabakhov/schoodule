/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement;

import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.enums.CurriculumPartType;
import com.eshabakhov.schoodule.federal.curriculum.requirement.input.MdRqInput;
import com.eshabakhov.schoodule.tables.FederalCurriculumRequirement;
import java.util.Map;
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
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return this.ctx.selectFrom(RqPostgres.REQUIREMENT)
            .where(RqPostgres.REQUIREMENT.ID.eq(this.rid)).fetchSingle(
                record -> media
                    .with("id", record.getId())
                    .with("grade", record.getGrade())
                    .with("subjectName", record.getSubjectName())
                    .with("weeklyHours", record.getWeeklyHours())
                    .with("partType", record.getPartType().name())
            );
    }

    @Override
    public Requirement revise(final RequirementInput input) throws Exception {
        final Map<String, String> fields = input.print(
            new MdRqInput()
        ).print();
        return new RqPostgres(
            this.ctx,
            this.ctx.update(RqPostgres.REQUIREMENT).set(
                RqPostgres.REQUIREMENT.GRADE,
                Integer.valueOf(fields.get("grade"))
            ).set(
                RqPostgres.REQUIREMENT.SUBJECT_NAME,
                fields.get("subjectName")
            ).set(
                RqPostgres.REQUIREMENT.WEEKLY_HOURS,
                Integer.valueOf(fields.get("weeklyHours"))
            ).set(
                RqPostgres.REQUIREMENT.PART_TYPE,
                CurriculumPartType.valueOf(fields.get("partType"))
            ).where(RqPostgres.REQUIREMENT.ID.eq(this.rid))
                .returningResult(RqPostgres.REQUIREMENT.ID)
                .fetchOne(RqPostgres.REQUIREMENT.ID)
        );
    }
}
