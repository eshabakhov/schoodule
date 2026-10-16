/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school;

import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.School;
import com.eshabakhov.schoodule.Schools;
import org.jooq.Condition;

/**
 * Brief schools representation for shared browser controls.
 *
 * @since 0.0.1
 */
public final class SlsBrief implements Schools {

    /** Origin schools. */
    private final Schools origin;

    /**
     * New brief representation.
     *
     * @param origin Origin schools
     */
    public SlsBrief(final Schools origin) {
        this.origin = origin;
    }

    @Override
    public School create(final String name) throws Exception {
        return this.origin.create(name);
    }

    @Override
    public School school(final long id) throws Exception {
        return this.origin.school(id);
    }

    @Override
    public School school(final String name) throws Exception {
        return this.origin.school(name);
    }

    @Override
    public PageableList<School> schools(
        final Condition condition,
        final Page page
    ) throws Exception {
        return this.origin.schools(condition, page);
    }

    @Override
    public void remove(final long school) throws Exception {
        this.origin.remove(school);
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return this.origin.print(media).include("href");
    }
}
