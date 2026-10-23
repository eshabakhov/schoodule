/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.teacher;

import com.eshabakhov.surok.Filters;
import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.Page;
import com.eshabakhov.surok.Sorts;
import java.util.List;

/**
 * Teachers representation containing one selected teacher.
 *
 * @since 0.0.1
 */
public final class ThsSingle implements Teachers {

    /** Origin teachers. */
    private final Teachers origin;

    /** Selected teacher. */
    private final Teacher selected;

    /**
     * New single-teacher representation.
     *
     * @param origin Origin teachers
     * @param selected Selected teacher
     */
    public ThsSingle(final Teachers origin, final Teacher selected) {
        this.origin = origin;
        this.selected = selected;
    }

    @Override
    public Teacher create(final TeacherInput input) throws Exception {
        return this.origin.create(input);
    }

    @Override
    public Teacher teacher(final long id) throws Exception {
        return this.origin.teacher(id);
    }

    @Override
    public Teachers selection(
        final Filters filters,
        final Page page,
        final Sorts sorts
    ) throws Exception {
        return this.origin.selection(filters, page, sorts);
    }

    @Override
    public Iterable<Teacher> iterate() {
        return List.of(this.selected);
    }

    @Override
    public void remove(final long id) throws Exception {
        this.origin.remove(id);
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return media.with("teacher", this.selected);
    }
}
