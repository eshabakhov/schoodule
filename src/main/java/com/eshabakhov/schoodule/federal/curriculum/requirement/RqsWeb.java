/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement;

import com.eshabakhov.schoodule.Filters;
import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.PrItems;
import com.eshabakhov.schoodule.Sorts;
import com.eshabakhov.schoodule.filter.FlWeb;
import com.eshabakhov.schoodule.sort.StWeb;

/**
 * Browser representation of curriculum requirements.
 *
 * @since 0.0.1
 */
public final class RqsWeb implements Requirements {

    /** Origin requirements. */
    private final Requirements origin;

    /** Applied filters. */
    private final Filters filters;

    /** Applied sorts. */
    private final Sorts sorts;

    /**
     * New browser representation.
     *
     * @param origin Origin requirements
     * @param filters Applied filters
     * @param sorts Applied sorts
     */
    public RqsWeb(
        final Requirements origin,
        final Filters filters,
        final Sorts sorts
    ) {
        this.origin = origin;
        this.filters = filters;
        this.sorts = sorts;
    }

    @Override
    public Requirement create(
        final Integer grade,
        final String subject,
        final Integer hours,
        final Requirement.PartType part
    ) throws Exception {
        return this.origin.create(grade, subject, hours, part);
    }

    @Override
    public Requirement requirement(final long id) throws Exception {
        return this.origin.requirement(id);
    }

    @Override
    public Requirements selection(
        final Filters selected,
        final Page page,
        final Sorts ordered
    ) throws Exception {
        return this.origin.selection(selected, page, ordered);
    }

    @Override
    public Iterable<Requirement> iterate() {
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
        ).with(
            "sorts",
            new PrItems(
                "item",
                this.sorts.sorts().stream().map(StWeb::new).toList()
            )
        );
    }
}
