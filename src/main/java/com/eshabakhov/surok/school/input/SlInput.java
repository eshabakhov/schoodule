/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.input;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.school.SchoolInput;

/**
 * Empty school input.
 *
 * @since 0.0.1
 */
public final class SlInput implements SchoolInput {

    /** New empty input. */
    public SlInput() {
        // Intentionally empty.
    }

    @Override
    public <T> Media<T> print(final Media<T> media) {
        return media;
    }
}
