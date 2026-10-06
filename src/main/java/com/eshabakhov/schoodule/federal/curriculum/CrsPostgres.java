/*
 * В© 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum;

import com.eshabakhov.schoodule.Filters;
import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.Sorts;
import com.eshabakhov.schoodule.enums.EducationLevelType;
import com.eshabakhov.schoodule.enums.StudyWeekType;
import com.eshabakhov.schoodule.federal.curriculum.filter.FlsCdCurriculum;
import com.eshabakhov.schoodule.federal.curriculum.sort.StsJqCurriculum;
import com.eshabakhov.schoodule.page.ResponsePage;
import com.eshabakhov.schoodule.tables.FederalCurriculum;
import com.eshabakhov.schoodule.tables.records.FederalCurriculumRecord;
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
    private static final FederalCurriculum CURRICULUM =
        FederalCurriculum.FEDERAL_CURRICULUM;

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
    public Curriculum create(
        final String title,
        final Curriculum.Level level,
        final Curriculum.Week week,
        final String version,
        final String year,
        final String description
    ) throws Exception {
        return this.ctx.transactionResult(
            config -> {
                final DSLContext ttx = DSL.using(config);
                if (ttx.selectFrom(CrsPostgres.CURRICULUM).where(
                    CrsPostgres.CURRICULUM.EDUCATION_LEVEL.eq(
                        EducationLevelType.valueOf(level.name())
                    ).and(
                        CrsPostgres.CURRICULUM.STUDY_WEEK_TYPE.eq(
                            StudyWeekType.valueOf(week.name())
                        )
                    ).and(CrsPostgres.CURRICULUM.VERSION.eq(version))
                        .and(CrsPostgres.CURRICULUM.ACADEMIC_YEAR.eq(year))
                        .and(CrsPostgres.CURRICULUM.IS_DELETED.eq(false))
                ).fetchOne() != null) {
                    throw new CurriculumAlreadyExistsException(
                        String.format(
                            "Curriculum `%s` already exists",
                            title
                        )
                    );
                }
                final FederalCurriculumRecord created = ttx
                    .insertInto(CrsPostgres.CURRICULUM)
                    .set(CrsPostgres.CURRICULUM.TITLE, title).set(
                        CrsPostgres.CURRICULUM.EDUCATION_LEVEL,
                        EducationLevelType.valueOf(level.name())
                    ).set(
                        CrsPostgres.CURRICULUM.STUDY_WEEK_TYPE,
                        StudyWeekType.valueOf(week.name())
                    )
                    .set(CrsPostgres.CURRICULUM.VERSION, version)
                    .set(CrsPostgres.CURRICULUM.ACADEMIC_YEAR, year)
                    .set(CrsPostgres.CURRICULUM.DESCRIPTION, description)
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
    public <M extends Media> M print(final M media) {
        media.with("items", this.iterate());
        return media;
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
