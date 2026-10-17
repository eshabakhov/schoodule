/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum;

import com.eshabakhov.schoodule.Filters;
import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.PrItems;
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

    /**
     * Complete collection.
     */
    private final Curriculums origin;

    /**
     * Selected curriculums.
     */
    private final Iterable<Curriculum> items;

    /**
     * Selection pagination.
     */
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
    public Curriculum create(final CurriculumInput input) throws Exception {
        return this.origin.create(input);
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
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return media.with(new PrItems("curriculum", this.items))
            .with("page", this.page);
    }

    @Override
    public void remove(final long id) throws Exception {
        this.origin.remove(id);
    }
}
