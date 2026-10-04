/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum;

import com.eshabakhov.schoodule.Filters;
import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.ResultPage;
import com.eshabakhov.schoodule.Sorts;
import com.eshabakhov.schoodule.federal.FederalCurriculum;
import com.eshabakhov.schoodule.federal.FederalCurriculums;
import lombok.EqualsAndHashCode;

/**
 * Selected federal curriculums.
 *
 * @since 0.0.1
 */
@EqualsAndHashCode
public final class FcsSelected implements FederalCurriculums {

    /** Complete collection. */
    private final FederalCurriculums origin;

    /** Selected curriculums. */
    private final Iterable<FederalCurriculum> items;

    /** Selection pagination. */
    private final ResultPage page;

    /**
     * New selected curriculums.
     *
     * @param origin Complete collection
     * @param items Selected curriculums
     * @param page Selection pagination
     */
    public FcsSelected(
        final FederalCurriculums origin,
        final Iterable<FederalCurriculum> items,
        final ResultPage page
    ) {
        this.origin = origin;
        this.items = items;
        this.page = page;
    }

    @Override
    public FederalCurriculum create(
        final String title,
        final FederalCurriculum.Level level,
        final FederalCurriculum.Week week,
        final String version,
        final String year,
        final String description
    ) throws Exception {
        return this.origin.create(title, level, week, version, year, description);
    }

    @Override
    public FederalCurriculum curriculum(final long id) throws Exception {
        return this.origin.curriculum(id);
    }

    @Override
    public FederalCurriculums selection(
        final Filters filters,
        final Page requested,
        final Sorts sorts
    ) throws Exception {
        return this.origin.selection(filters, requested, sorts);
    }

    @Override
    public Iterable<FederalCurriculum> iterate() {
        return this.items;
    }

    @Override
    public <M extends Media> M print(final M media) {
        media.with("items", this.items).with("page", this.page);
        return media;
    }

    @Override
    public void remove(final long id) throws Exception {
        this.origin.remove(id);
    }
}
