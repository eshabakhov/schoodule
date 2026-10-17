/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum;

import com.eshabakhov.schoodule.Filters;
import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.PrItems;
import com.eshabakhov.schoodule.Sorts;
import com.eshabakhov.schoodule.filter.FlWeb;

/**
 * Browser representation of federal curriculums.
 *
 * @since 0.0.1
 */
public final class CrsWeb implements Curriculums {

    /** Origin curriculums. */
    private final Curriculums origin;

    /** Applied filters. */
    private final Filters filters;

    /**
     * New browser representation.
     *
     * @param origin Origin curriculums
     * @param filters Applied filters
     */
    public CrsWeb(final Curriculums origin, final Filters filters) {
        this.origin = origin;
        this.filters = filters;
    }

    @Override
    public Curriculum create(final CurriculumInput input) throws Exception {
        return this.origin.create(input);
    }

    @Override
    public Curriculum curriculum(final long id) throws Exception {
        return this.origin.curriculum(id);
    }

    @Override
    public Curriculums selection(
        final Filters selected,
        final Page page,
        final Sorts sorts
    ) throws Exception {
        return this.origin.selection(selected, page, sorts);
    }

    @Override
    public Iterable<Curriculum> iterate() {
        return this.origin.iterate();
    }

    @Override
    public void remove(final long id) throws Exception {
        this.origin.remove(id);
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return this.origin.print(media).with(
            "filters",
            new PrItems(
                "item",
                this.filters.filters().stream().map(FlWeb::new).toList()
            )
        );
    }
}
