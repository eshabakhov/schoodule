/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.schedule;

import com.eshabakhov.surok.Filters;
import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.Page;
import com.eshabakhov.surok.Sorts;
import java.util.List;

/**
 * Schedules representation containing one selected schedule.
 *
 * @since 0.0.1
 */
public final class SdsSingle implements Schedules {

    /** Origin schedules. */
    private final Schedules origin;

    /** Selected schedule. */
    private final Schedule selected;

    /**
     * New single-schedule representation.
     *
     * @param origin Origin schedules
     * @param selected Selected schedule
     */
    public SdsSingle(final Schedules origin, final Schedule selected) {
        this.origin = origin;
        this.selected = selected;
    }

    @Override
    public Schedule create(final ScheduleInput input) throws Exception {
        return this.origin.create(input);
    }

    @Override
    public Schedule schedule(final long id) throws Exception {
        return this.origin.schedule(id);
    }

    @Override
    public Schedules selection(
        final Filters filters,
        final Page page,
        final Sorts sorts
    ) throws Exception {
        return this.origin.selection(filters, page, sorts);
    }

    @Override
    public Iterable<Schedule> iterate() {
        return List.of(this.selected);
    }

    @Override
    public void remove(final long id) throws Exception {
        this.origin.remove(id);
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return media.with("schedule", this.selected);
    }
}
