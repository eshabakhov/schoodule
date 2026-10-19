/*
 * В© 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.federal.curriculum.requirement;

import com.eshabakhov.surok.Filters;
import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.Page;
import com.eshabakhov.surok.PrItems;
import com.eshabakhov.surok.Sorts;
import com.eshabakhov.surok.enums.CurriculumPartType;
import com.eshabakhov.surok.federal.curriculum.requirement.filter.FlsCdRequirement;
import com.eshabakhov.surok.federal.curriculum.requirement.input.MdRqInput;
import com.eshabakhov.surok.federal.curriculum.requirement.sort.StsJqRequirement;
import com.eshabakhov.surok.page.ResponsePage;
import com.eshabakhov.surok.tables.FederalCurriculumRequirement;
import com.eshabakhov.surok.tables.records.FederalCurriculumRequirementRecord;
import java.util.Map;
import lombok.EqualsAndHashCode;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

/**
 * Postgres implementation of {@link Requirements}.
 *
 * @since 0.0.1
 * @checkstyle LambdaBodyLengthCheck (1000 lines)
 */
@EqualsAndHashCode
public final class RqsPostgres implements Requirements {

    /**
     * JOOQ Table for Requirement.
     */
    private static final FederalCurriculumRequirement REQUIREMENT =
        FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT;

    /**
     * JOOQ DSL context for executing database queries.
     */
    private final DSLContext ctx;

    /**
     * Federal curriculum ID.
     */
    private final Long fid;

    /**
     * Ctor.
     *
     * @param ctx JOOQ DSL context
     * @param fid Federal curriculum ID
     * @since 0.0.1
     */
    public RqsPostgres(final DSLContext ctx, final Long fid) {
        this.ctx = ctx;
        this.fid = fid;
    }

    @Override
    public Requirement create(final RequirementInput input) throws Exception {
        final Map<String, String> fields = input.print(
            new MdRqInput()
        ).print();
        final Integer grade = Integer.valueOf(fields.get("grade"));
        final CurriculumPartType part = CurriculumPartType.valueOf(
            fields.get("partType")
        );
        return this.ctx.transactionResult(
            config -> {
                final DSLContext ttx = DSL.using(config);
                if (ttx.selectFrom(RqsPostgres.REQUIREMENT).where(
                    RqsPostgres.REQUIREMENT.FEDERAL_CURRICULUM_ID.eq(this.fid)
                        .and(RqsPostgres.REQUIREMENT.GRADE.eq(grade)).and(
                            RqsPostgres.REQUIREMENT.SUBJECT_NAME.eq(
                                fields.get("subjectName")
                            )
                        ).and(
                            RqsPostgres.REQUIREMENT.PART_TYPE.eq(part)
                        ).and(
                            RqsPostgres.REQUIREMENT.IS_DELETED.eq(false)
                        )
                    )
                    .fetchOne() != null) {
                    throw new RequirementAlreadyExistsException();
                }
                final FederalCurriculumRequirementRecord created = ttx
                    .insertInto(RqsPostgres.REQUIREMENT).set(
                        RqsPostgres.REQUIREMENT.FEDERAL_CURRICULUM_ID,
                        this.fid
                    ).set(
                        RqsPostgres.REQUIREMENT.GRADE,
                        grade
                    ).set(
                        RqsPostgres.REQUIREMENT.SUBJECT_NAME,
                        fields.get("subjectName")
                    ).set(
                        RqsPostgres.REQUIREMENT.WEEKLY_HOURS,
                        Integer.valueOf(fields.get("weeklyHours"))
                    ).set(
                        RqsPostgres.REQUIREMENT.PART_TYPE,
                        part
                    )
                    .set(RqsPostgres.REQUIREMENT.IS_DELETED, false)
                    .returning()
                    .fetchOne();
                if (created == null) {
                    throw new RequirementFailedCreateException();
                }
                return new RqPostgres(this.ctx, created.getId());
            }
        );
    }

    @Override
    public Requirement requirement(final long id) throws Exception {
        final FederalCurriculumRequirementRecord selected = this.ctx
            .selectFrom(RqsPostgres.REQUIREMENT).where(
                RqsPostgres.REQUIREMENT.FEDERAL_CURRICULUM_ID.eq(this.fid)
                    .and(RqsPostgres.REQUIREMENT.IS_DELETED.eq(false))
                    .and(RqsPostgres.REQUIREMENT.ID.eq(id))
            )
            .fetchOne();
        if (selected == null) {
            throw new RequirementNotFoundException(
                String.format("Requirement with id=%d not found", id)
            );
        }
        return new RqPostgres(this.ctx, selected.getId());
    }

    @Override
    public Requirements selection(
        final Filters filters,
        final Page page,
        final Sorts sorts
    ) throws Exception {
        final Condition scoped = RqsPostgres.REQUIREMENT.FEDERAL_CURRICULUM_ID.eq(this.fid)
            .and(RqsPostgres.REQUIREMENT.IS_DELETED.eq(false))
            .and(new FlsCdRequirement(filters).condition());
        return new RqsSelected(
            this,
            this.ctx
                .selectFrom(RqsPostgres.REQUIREMENT)
                .where(scoped)
                .orderBy(new StsJqRequirement(sorts).fields())
                .limit(page.limit())
                .offset((page.offset() - 1) * page.limit()).fetch(
                    selected ->
                        new RqPostgres(this.ctx, selected.getId())
                ),
            new ResponsePage(
                page,
                this.ctx.fetchCount(
                    this.ctx.selectFrom(RqsPostgres.REQUIREMENT).where(scoped)
                )
            )
        );
    }

    @Override
    public Iterable<Requirement> iterate() {
        return this.ctx
            .selectFrom(RqsPostgres.REQUIREMENT).where(
                RqsPostgres.REQUIREMENT.FEDERAL_CURRICULUM_ID.eq(this.fid)
                    .and(RqsPostgres.REQUIREMENT.IS_DELETED.eq(false))
            )
            .fetch(selected -> new RqPostgres(this.ctx, selected.getId()));
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return media.with(new PrItems("requirement", this.iterate()));
    }

    @Override
    public void remove(final long id) throws Exception {
        if (this.ctx
            .selectFrom(RqsPostgres.REQUIREMENT).where(
                RqsPostgres.REQUIREMENT.FEDERAL_CURRICULUM_ID.eq(this.fid)
                    .and(RqsPostgres.REQUIREMENT.IS_DELETED.eq(false))
                    .and(RqsPostgres.REQUIREMENT.ID.eq(id))
            )
            .fetchOne() == null) {
            throw new RequirementNotFoundException(
                String.format(
                    "Requirement with id=%d not found",
                    id
                )
            );
        }
        this.ctx.transactionResult(
            config ->
                DSL.using(config)
                    .update(RqsPostgres.REQUIREMENT)
                    .set(RqsPostgres.REQUIREMENT.IS_DELETED, true)
                    .set(RqsPostgres.REQUIREMENT.UPDATED_AT, DSL.currentOffsetDateTime())
                    .where(RqsPostgres.REQUIREMENT.ID.eq(id))
                    .execute()
        );
    }
}
