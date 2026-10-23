/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.schoolclass.input;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.school.schoolclass.SchoolClassInput;

/**
 * School class input with a grade.
 *
 * @since 0.0.1
 */
public final class ScInputGrade implements SchoolClassInput {

    /** Origin input. */
    private final SchoolClassInput origin;

    /** Requested grade. */
    private final String grade;

    /**
     * New grade input.
     *
     * @param origin Origin input
     * @param grade Requested grade
     */
    public ScInputGrade(final SchoolClassInput origin, final String grade) {
        this.origin = origin;
        this.grade = grade;
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        if (this.grade == null || !this.grade.trim().matches("[1-9]|1[01]")) {
            throw new SchoolClassInputException("School class grade must be between 1 and 11");
        }
        return this.origin.print(media).with("grade", Integer.valueOf(this.grade.trim()));
    }
}
