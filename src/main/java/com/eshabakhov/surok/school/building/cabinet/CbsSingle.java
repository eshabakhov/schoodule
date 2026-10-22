/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.building.cabinet;

import com.eshabakhov.surok.Filters;
import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.Page;
import com.eshabakhov.surok.Sorts;
import java.util.List;

/**
 * Cabinets representation containing one selected cabinet.
 *
 * @since 0.0.1
 */
public final class CbsSingle implements Cabinets {

    /** Origin cabinets. */
    private final Cabinets origin;

    /** Selected cabinet. */
    private final Cabinet selected;

    /**
     * New single-cabinet representation.
     *
     * @param origin Origin cabinets
     * @param selected Selected cabinet
     */
    public CbsSingle(final Cabinets origin, final Cabinet selected) {
        this.origin = origin;
        this.selected = selected;
    }

    @Override
    public Cabinet create(final CabinetInput input) throws Exception {
        return this.origin.create(input);
    }

    @Override
    public Cabinet cabinet(final long id) throws Exception {
        return this.origin.cabinet(id);
    }

    @Override
    public Cabinets selection(
        final Filters filters,
        final Page page,
        final Sorts sorts
    ) throws Exception {
        return this.origin.selection(filters, page, sorts);
    }

    @Override
    public Iterable<Cabinet> iterate() {
        return List.of(this.selected);
    }

    @Override
    public void remove(final long id) throws Exception {
        this.origin.remove(id);
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return media.with("cabinet", this.selected);
    }
}
