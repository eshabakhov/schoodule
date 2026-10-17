/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum;

import com.eshabakhov.schoodule.Filters;
import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.Sorts;
import java.util.List;

/**
 * Curriculums representation containing one selected curriculum.
 *
 * @since 0.0.1
 */
public final class CrsSingle implements Curriculums {

    /** Origin curriculums. */
    private final Curriculums origin;

    /** Selected curriculum. */
    private final Curriculum selected;

    /**
     * New single-curriculum representation.
     *
     * @param origin Origin curriculums
     * @param selected Selected curriculum
     */
    public CrsSingle(final Curriculums origin, final Curriculum selected) {
        this.origin = origin;
        this.selected = selected;
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
        final Page page,
        final Sorts sorts
    ) throws Exception {
        return this.origin.selection(filters, page, sorts);
    }

    @Override
    public Iterable<Curriculum> iterate() {
        return List.of(this.selected);
    }

    @Override
    public void remove(final long id) throws Exception {
        this.origin.remove(id);
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return media.with("curriculum", this.selected);
    }
}
