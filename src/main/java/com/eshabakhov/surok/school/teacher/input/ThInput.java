/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.teacher.input;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.school.teacher.TeacherInput;

/**
 * Empty school teacher input.
 *
 * @since 0.0.1
 */
public final class ThInput implements TeacherInput {

    /**
     * New empty input.
     */
    public ThInput() {
        // Intentionally empty.
    }

    @Override
    public <T> Media<T> print(final Media<T> media) {
        return media;
    }
}
