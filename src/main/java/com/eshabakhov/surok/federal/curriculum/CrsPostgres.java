/*
 * В© 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.federal.curriculum;

import com.eshabakhov.surok.Filters;
import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.Page;
import com.eshabakhov.surok.PrItems;
import com.eshabakhov.surok.Sorts;
import com.eshabakhov.surok.enums.EducationLevelType;
import com.eshabakhov.surok.enums.StudyWeekType;
import com.eshabakhov.surok.federal.curriculum.filter.FlsCdCurriculum;
import com.eshabakhov.surok.federal.curriculum.input.MdCrInput;
import com.eshabakhov.surok.federal.curriculum.sort.StsJqCurriculum;
import com.eshabakhov.surok.page.ResponsePage;
import com.eshabakhov.surok.tables.FederalCurriculum;
import com.eshabakhov.surok.tables.records.FederalCurriculumRecord;
import java.util.Map;
import lombok.EqualsAndHashCode;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

/**
 * Postgres implementation of {@link Curriculums}.
 *
 * @since 0.0.1
 * @checkstyle LambdaBodyLengthCheck (1000 lines)
 */
@EqualsAndHashCode
public final class CrsPostgres implements Curriculums {

    /**
     * JOOQ Table for Curriculum.
     */
    private static final FederalCurriculum CURRICULUM = FederalCurriculum.FEDERAL_CURRICULUM;

    /**
     * JOOQ DSL context for executing database queries.
     */
    private final DSLContext ctx;

    /**
     * Ctor.
     *
     * @param ctx JOOQ DSL context
     * @since 0.0.1
     */
    public CrsPostgres(final DSLContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public Curriculum create(final CurriculumInput input) throws Exception {
        final Map<String, String> fields = input.print(
            new MdCrInput()
        ).print();
        final EducationLevelType level = EducationLevelType.valueOf(
            fields.get("level")
        );
        final StudyWeekType week = StudyWeekType.valueOf(
            fields.get("week")
        );
        return this.ctx.transactionResult(
            config -> {
                final DSLContext ttx = DSL.using(config);
                if (ttx.selectFrom(CrsPostgres.CURRICULUM).where(
                    CrsPostgres.CURRICULUM.EDUCATION_LEVEL.eq(
                        level
                    ).and(
                        CrsPostgres.CURRICULUM.STUDY_WEEK_TYPE.eq(
                            week
                        )
                    ).and(
                        CrsPostgres.CURRICULUM.VERSION.eq(
                            fields.get("version")
                        )
                    ).and(
                        CrsPostgres.CURRICULUM.ACADEMIC_YEAR.eq(
                            fields.get("year")
                        )
                    ).and(CrsPostgres.CURRICULUM.IS_DELETED.eq(false))
                ).fetchOne() != null) {
                    throw new CurriculumAlreadyExistsException(
                        String.format(
                            "Curriculum `%s` already exists",
                            fields.get("title")
                        )
                    );
                }
                final FederalCurriculumRecord created = ttx
                    .insertInto(CrsPostgres.CURRICULUM).set(
                        CrsPostgres.CURRICULUM.TITLE,
                        fields.get("title")
                    ).set(
                        CrsPostgres.CURRICULUM.EDUCATION_LEVEL,
                        level
                    ).set(
                        CrsPostgres.CURRICULUM.STUDY_WEEK_TYPE,
                        week
                    ).set(
                        CrsPostgres.CURRICULUM.VERSION,
                        fields.get("version")
                    ).set(
                        CrsPostgres.CURRICULUM.ACADEMIC_YEAR,
                        fields.get("year")
                    ).set(
                        CrsPostgres.CURRICULUM.DESCRIPTION,
                        fields.get("description")
                    )
                    .set(CrsPostgres.CURRICULUM.IS_DELETED, false)
                    .returning()
                    .fetchOne();
                if (created == null) {
                    throw new CurriculumFailedCreateException();
                }
                return new CrPostgres(this.ctx, created.getId());
            }
        );
    }

    @Override
    public Curriculum curriculum(final long id) throws Exception {
        final FederalCurriculumRecord selected = this.ctx
            .selectFrom(CrsPostgres.CURRICULUM).where(
                CrsPostgres.CURRICULUM.ID.eq(id)
                    .and(CrsPostgres.CURRICULUM.IS_DELETED.eq(false))
            )
            .fetchOne();
        if (selected == null) {
            throw new CurriculumNotFoundException(
                String.format("Curriculum with id=%d not found", id)
            );
        }
        return new CrPostgres(this.ctx, selected.getId());
    }

    @Override
    public Curriculums selection(
        final Filters filters,
        final Page page,
        final Sorts sorts
    ) throws Exception {
        final Condition scoped = new FlsCdCurriculum(filters).condition().and(
            CrsPostgres.CURRICULUM.IS_DELETED.eq(false)
        );
        return new CrsSelected(
            this,
            this.ctx
                .selectFrom(CrsPostgres.CURRICULUM)
                .where(scoped)
                .orderBy(new StsJqCurriculum(sorts).fields())
                .limit(page.limit())
                .offset((page.offset() - 1) * page.limit()).fetch(
                    selected -> new CrPostgres(
                        this.ctx,
                        selected.getId()
                    )
                ),
            new ResponsePage(
                page,
                this.ctx.fetchCount(
                    this.ctx
                        .selectFrom(CrsPostgres.CURRICULUM)
                        .where(scoped)
                )
            )
        );
    }

    @Override
    public Iterable<Curriculum> iterate() {
        return this.ctx
            .selectFrom(CrsPostgres.CURRICULUM)
            .where(CrsPostgres.CURRICULUM.IS_DELETED.eq(false))
            .fetch(selected -> new CrPostgres(this.ctx, selected.getId()));
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return media.with(new PrItems("curriculum", this.iterate()));
    }

    @Override
    public void remove(final long id) throws Exception {
        if (this.ctx
            .selectFrom(CrsPostgres.CURRICULUM).where(
                CrsPostgres.CURRICULUM.ID.eq(id)
                    .and(CrsPostgres.CURRICULUM.IS_DELETED.eq(false))
            )
            .fetchOne() == null) {
            throw new CurriculumNotFoundException(
                String.format("Curriculum with id=%d not found", id)
            );
        }
        this.ctx.transactionResult(
            config ->
                DSL.using(config)
                    .update(CrsPostgres.CURRICULUM)
                    .set(CrsPostgres.CURRICULUM.IS_DELETED, true).set(
                        CrsPostgres.CURRICULUM.UPDATED_AT,
                        DSL.currentOffsetDateTime()
                    )
                    .where(CrsPostgres.CURRICULUM.ID.eq(id))
                    .execute()
        );
    }
}
