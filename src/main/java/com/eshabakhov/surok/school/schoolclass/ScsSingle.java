/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.schoolclass;

import com.eshabakhov.surok.Filters;
import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.Page;
import com.eshabakhov.surok.Sorts;
import java.util.List;

/**
 * SchoolClasses representation containing one selected class.
 *
 * @since 0.0.1
 */
public final class ScsSingle implements SchoolClasses {

    /** Origin classes. */
    private final SchoolClasses origin;

    /** Selected class. */
    private final SchoolClass selected;

    /**
     * New representation containing one school class.
     *
     * @param origin Origin classes
     * @param selected Selected class
     */
    public ScsSingle(final SchoolClasses origin, final SchoolClass selected) {
        this.origin = origin;
        this.selected = selected;
    }

    @Override
    public SchoolClass create(final SchoolClassInput input) throws Exception {
        return this.origin.create(input);
    }

    @Override
    public SchoolClass clazz(final long id) throws Exception {
        return this.origin.clazz(id);
    }

    @Override
    public SchoolClasses selection(
        final Filters filters,
        final Page page,
        final Sorts sorts
    ) throws Exception {
        return this.origin.selection(filters, page, sorts);
    }

    @Override
    public Iterable<SchoolClass> iterate() {
        return List.of(this.selected);
    }

    @Override
    public void remove(final long id) throws Exception {
        this.origin.remove(id);
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return media.with("school-class", this.selected);
    }
}
