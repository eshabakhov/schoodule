/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.teacher.input;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.school.teacher.TeacherInput;

/**
 * School teacher input with a name.
 *
 * @since 0.0.1
 */
public final class ThInputName implements TeacherInput {

    /** Origin input. */
    private final TeacherInput origin;

    /** Teacher name. */
    private final String name;

    /**
     * New name input.
     *
     * @param origin Origin input
     * @param name Teacher name
     */
    public ThInputName(final TeacherInput origin, final String name) {
        this.origin = origin;
        this.name = name;
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        if (this.name == null || this.name.isBlank()) {
            throw new TeacherInputException(
                "Teacher name is required"
            );
        }
        return this.origin.print(media).with("name", this.name.trim());
    }
}
