/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.schoolclass.input;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.school.schoolclass.SchoolClassInput;

/**
 * Empty school class input.
 *
 * @since 0.0.1
 */
public final class ScInput implements SchoolClassInput {

    /**
     * New empty input.
     */
    public ScInput() {
        // Intentionally empty.
    }

    @Override
    public <T> Media<T> print(final Media<T> media) {
        return media;
    }
}
