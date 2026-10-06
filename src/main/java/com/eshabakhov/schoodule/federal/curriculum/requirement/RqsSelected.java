/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement;

import com.eshabakhov.schoodule.Filters;
import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.ResultPage;
import com.eshabakhov.schoodule.Sorts;
import lombok.EqualsAndHashCode;

/**
 * Selected federal curriculum requirements.
 *
 * @since 0.0.1
 */
@EqualsAndHashCode
public final class RqsSelected implements Requirements {

    /** Complete collection. */
    private final Requirements origin;

    /** Selected requirements. */
    private final Iterable<Requirement> items;

    /** Selection pagination. */
    private final ResultPage page;

    /**
     * New selected requirements.
     *
     * @param origin Complete collection
     * @param items Selected requirements
     * @param page Selection pagination
     */
    public RqsSelected(
        final Requirements origin,
        final Iterable<Requirement> items,
        final ResultPage page
    ) {
        this.origin = origin;
        this.items = items;
        this.page = page;
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
        final Filters filters,
        final Page requested,
        final Sorts sorts
    ) throws Exception {
        return this.origin.selection(filters, requested, sorts);
    }

    @Override
    public Iterable<Requirement> iterate() {
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
