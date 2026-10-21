/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.building;

import com.eshabakhov.surok.Filters;
import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.Page;
import com.eshabakhov.surok.Sorts;
import java.util.List;

/**
 * Buildings representation containing one selected building.
 *
 * @since 0.0.1
 */
public final class BdsSingle implements Buildings {

    /** Origin buildings. */
    private final Buildings origin;

    /** Selected building. */
    private final Building selected;

    /**
     * New single-building representation.
     *
     * @param origin Origin buildings
     * @param selected Selected building
     */
    public BdsSingle(final Buildings origin, final Building selected) {
        this.origin = origin;
        this.selected = selected;
    }

    @Override
    public Building create(final BuildingInput input) throws Exception {
        return this.origin.create(input);
    }

    @Override
    public Building building(final long id) throws Exception {
        return this.origin.building(id);
    }

    @Override
    public Buildings selection(
        final Filters filters,
        final Page page,
        final Sorts sorts
    ) throws Exception {
        return this.origin.selection(filters, page, sorts);
    }

    @Override
    public Iterable<Building> iterate() {
        return List.of(this.selected);
    }

    @Override
    public void remove(final long id) throws Exception {
        this.origin.remove(id);
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return media.with("building", this.selected);
    }
}
