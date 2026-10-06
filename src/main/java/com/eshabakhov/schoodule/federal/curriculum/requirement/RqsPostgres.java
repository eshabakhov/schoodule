/*
 * В© 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement;

import com.eshabakhov.schoodule.Filters;
import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.Sorts;
import com.eshabakhov.schoodule.enums.CurriculumPartType;
import com.eshabakhov.schoodule.federal.curriculum.requirement.filter.FlsCdRequirement;
import com.eshabakhov.schoodule.federal.curriculum.requirement.sort.StsJqRequirement;
import com.eshabakhov.schoodule.page.ResponsePage;
import com.eshabakhov.schoodule.tables.FederalCurriculumRequirement;
import com.eshabakhov.schoodule.tables.records.FederalCurriculumRequirementRecord;
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
    public Requirement create(
        final Integer grade,
        final String subject,
        final Integer hours,
        final Requirement.PartType part
    ) throws Exception {
        return this.ctx.transactionResult(
            config -> {
                final DSLContext ttx = DSL.using(config);
                if (ttx.selectFrom(RqsPostgres.REQUIREMENT).where(
                    RqsPostgres.REQUIREMENT.FEDERAL_CURRICULUM_ID.eq(this.fid)
                        .and(RqsPostgres.REQUIREMENT.GRADE.eq(grade))
                        .and(RqsPostgres.REQUIREMENT.SUBJECT_NAME.eq(subject)).and(
                            RqsPostgres.REQUIREMENT.PART_TYPE.eq(
                                CurriculumPartType.valueOf(part.name())
                            )
                        )
                        .and(RqsPostgres.REQUIREMENT.IS_DELETED.eq(false))
                    )
                    .fetchOne() != null) {
                    throw new RequirementAlreadyExistsException();
                }
                final FederalCurriculumRequirementRecord created = ttx
                    .insertInto(RqsPostgres.REQUIREMENT)
                    .set(RqsPostgres.REQUIREMENT.FEDERAL_CURRICULUM_ID, this.fid)
                    .set(RqsPostgres.REQUIREMENT.GRADE, grade)
                    .set(RqsPostgres.REQUIREMENT.SUBJECT_NAME, subject)
                    .set(RqsPostgres.REQUIREMENT.WEEKLY_HOURS, hours).set(
                        RqsPostgres.REQUIREMENT.PART_TYPE,
                        CurriculumPartType.valueOf(part.name())
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
    public <M extends Media> M print(final M media) {
        media.with("items", this.iterate());
        return media;
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
