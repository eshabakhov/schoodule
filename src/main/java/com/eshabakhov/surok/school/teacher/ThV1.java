/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.teacher;

import com.eshabakhov.surok.Media;

/**
 * Version 1 implementation of {@link Teacher}.
 *
 * @since 0.0.1
 */
public final class ThV1 implements Teacher {

    /**
     * Original teacher.
     */
    private final Teacher origin;

    /**
     * Creates a version 1 teacher.
     *
     * @param origin Original teacher
     */
    public ThV1(final Teacher origin) {
        this.origin = origin;
    }

    @Override
    public Long uid() {
        return this.origin.uid();
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return this.origin.print(media).include(
            "id", "name"
        );
    }

    @Override
    public Teacher revise(final TeacherInput input) throws Exception {
        return this.origin.revise(input);
    }
}
