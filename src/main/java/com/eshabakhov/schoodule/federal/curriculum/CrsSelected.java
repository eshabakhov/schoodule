/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum;

import com.eshabakhov.schoodule.Filters;
import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.ResultPage;
import com.eshabakhov.schoodule.Sorts;
import lombok.EqualsAndHashCode;

/**
 * Selected federal curriculums.
 *
 * @since 0.0.1
 */
@EqualsAndHashCode
public final class CrsSelected implements Curriculums {

    /** Complete collection. */
    private final Curriculums origin;

    /** Selected curriculums. */
    private final Iterable<Curriculum> items;

    /** Selection pagination. */
    private final ResultPage page;

    /**
     * New selected curriculums.
     *
     * @param origin Complete collection
     * @param items Selected curriculums
     * @param page Selection pagination
     */
    public CrsSelected(
        final Curriculums origin,
        final Iterable<Curriculum> items,
        final ResultPage page
    ) {
        this.origin = origin;
        this.items = items;
        this.page = page;
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
        return this.origin.create(title, level, week, version, year, description);
    }

    @Override
    public Curriculum curriculum(final long id) throws Exception {
        return this.origin.curriculum(id);
    }

    @Override
    public Curriculums selection(
        final Filters filters,
        final Page requested,
        final Sorts sorts
    ) throws Exception {
        return this.origin.selection(filters, requested, sorts);
    }

    @Override
    public Iterable<Curriculum> iterate() {
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
